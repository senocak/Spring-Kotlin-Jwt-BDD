# 🥒Cucumber Integration Testing in Spring Boot Kotlin JWT Application

This guide demonstrates how to implement robust Behavior-Driven Development (BDD) integration tests using Cucumber in a Spring Boot Kotlin application with JWT authentication. The setup leverages Testcontainers for database isolation and provides a clean, maintainable testing architecture.

## 🎯Why Cucumber for Integration Testing?

Cucumber enables writing human-readable feature specifications that directly map to automated tests. This approach:

- **Bridges the gap** between technical and non-technical stakeholders
- **Ensures requirements** are clear and testable
- **Provides living documentation** that stays in sync with code
- **Enables BDD workflows** for better collaboration

## 🏗️Project Structure

```
src/test/
├── kotlin/com/github/senocak/boilerplate/
│   ├── config/
│   │   ├── CucumberTestConfig.kt          # Main test configuration
│   │   └── initializer/
│   │       └── PostgresqlInitializer.kt   # Database setup
│   └── stepdefs/
│       ├── CucumberBase.kt                # Base class with HTTP utilities
│       └── AuthSteps.kt                   # Authentication step definitions
├── resources/
│   ├── application-cucumber-test.yml      # Test-specific configuration
│   └── features/
│       └── Auth.feature                   # Gherkin feature files
```

## 💡How It Works: Complete End-to-End Example

Let's walk through the complete flow of how Cucumber integration testing works in this project:

### 1️⃣Writing Feature Files
Feature files are written in Gherkin syntax to describe test scenarios in plain English. This makes them readable by non-technical stakeholders while providing structure for test automation. They are stored in `src/test/resources/features/`:

```gherkin
Feature: Feature: User Authentication
  Scenario: client makes call to POST to login
    When the client calls "/login" with username "asenocakAdmin" and password "asenocak" and cast to "com.github.senocak.boilerplate.domain.dto.UserWrapperResponse"
    Then the client receives status code of 200
    Then response has field "token"
```
This feature file describes a scenario where:
1. A client sends login credentials to the `/login` endpoint
2. We expect a 200 OK response
3. The response should contain a JWT token

### 2️⃣Setting Up the Test Infrastructure

#### Cucumber Test Configuration

The `CucumberTestConfig.kt` annotation consolidates all the necessary configuration for our tests:

```kotlin
@Tag(value = "cucumber")
@Target(allowedTargets = [AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS])
@Retention(value = AnnotationRetention.RUNTIME)
@ActiveProfiles(value = ["cucumber-test"])
@TestClassOrder(value = ClassOrderer.OrderAnnotation::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(initializers = [
    PostgresqlInitializer::class,
])
@TestPropertySource(value = ["/application-cucumber-test.yml"])
@Suite
@CucumberContextConfiguration
@IncludeEngines(value = ["cucumber"])
@SelectClasspathResource(value = "features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.github.senocak.boilerplate.stepdefs")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
annotation class CucumberTestConfig
```

Key annotations:
- **`@ActiveProfiles("cucumber-test")`** - Uses a dedicated test profile
- **`@SpringBootTest(webEnvironment = RANDOM_PORT)`** - Starts the full application with a random port
- **`@ContextConfiguration(initializers = [PostgresqlInitializer::class])`** - Sets up a test database
- **`@SelectClasspathResource("features")`** - Locates Gherkin feature files
- **`@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.github.senocak.boilerplate.stepdefs")`** - Connects feature files to step definitions
- **`@CucumberContextConfiguration`** - Integrates Cucumber with Spring Boot
- **`@Transactional(propagation = NOT_SUPPORTED)`** - Prevents transaction rollback

#### Database Setup with Testcontainers

The `PostgresqlInitializer.kt` class creates and initializes an isolated PostgreSQL container:

```kotlin
@TestConfiguration
class PostgresqlInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val postgresContainer: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:14").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        withInitScripts("migration/V1__init.sql", "migration/V2__populate.sql")
        withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT)
    }

    init {
        postgresContainer.start()
    }

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=" + postgresContainer.jdbcUrl,
            "spring.datasource.username=" + postgresContainer.username,
            "spring.datasource.password=" + postgresContainer.password
        ).applyTo(configurableApplicationContext.environment)
    }
}
```

This provides:
- A fresh database for each test run
- Isolation from development or production environments
- Execution of migration scripts for schema and test data

### 3️⃣Building the Testing Foundation

#### Base Class (`CucumberBase.kt`)

The `CucumberBase` class provides the core HTTP testing functionality:

```kotlin
@CucumberTestConfig
open class CucumberBase {
    @LocalServerPort protected var localPort: Int = 0
    @Autowired protected lateinit var objectMapper: ObjectMapper
    @Autowired protected lateinit var restTemplateBuilder: RestTemplateBuilder
    protected lateinit var restTemplate: RestTemplate
    protected lateinit var latestResponse: ResponseResults
    val headers: MutableMap<String, String> = hashMapOf("Accept" to "application/json")
    
    // HTTP request methods (GET, POST, PUT, DELETE)
    // Response handling and parsing
}
```

**Key Features:**
- **Dynamic port injection** with `@LocalServerPort`
- **Jackson integration** for JSON parsing with `ObjectMapper`
- **RestTemplate configuration** for HTTP requests
- **Response caching** for step-to-step data sharing
- **Custom error handling** with `ResponseResultErrorHandler`
- **Request headers** management

#### HTTP Request Methods

The base class provides methods for all HTTP verbs:

```kotlin
@Throws(exceptionClasses = [IOException::class])
fun executePost(url: String, entries: MutableMap<String, Any>, classToCast: Class<*>) {
    headers.put("Content-Type", "application/json")
    val requestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
    requestCallback.setBody(JSONObject(entries).toString())
    val errorHandler = ResponseResultErrorHandler()
    restTemplate.setErrorHandler(errorHandler)
    latestResponse = restTemplate.execute(url, HttpMethod.POST, requestCallback,
        { response: ClientHttpResponse ->
            when {
                errorHandler.hadError -> errorHandler.results
                else -> ResponseResults(theResponse = response, classToCast = classToCast)
            }
        }) ?: throw IOException("Response was null for URL: $url")
}
```
- `executeGet(url: String)` - GET requests
- `executePost(url, entries, classToCast)` - POST with JSON body
- `executePut(url, entries)` - PUT requests
- `executeDelete(url)` - DELETE requests

These methods:
- Set appropriate headers
- Marshal request bodies to JSON
- Handle error responses
- Parse and store the response for assertions

#### Custom Error Handling

```kotlin
class ResponseResultErrorHandler: ResponseErrorHandler {
    var results: ResponseResults? = null
    var hadError = false
    
    override fun hasError(response: ClientHttpResponse): Boolean {
        hadError = response.statusCode.value() >= 500
        return hadError
    }
}
```
This captures both successful and error responses for comprehensive testing.

#### Custom Response Handling

The `ResponseResults` class captures and processes HTTP responses:
```kotlin
class ResponseResults internal constructor(
    val theResponse: ClientHttpResponse,
    val classToCast: Class<*>? = null,
) {
    var body: String = toString(inputStream = theResponse.body)

    private fun toString(inputStream: InputStream): String {
        val reader = BufferedReader(inputStream.reader())
        val content = StringBuilder()
        try {
            var line = reader.readLine()
            while (line != null) {
                content.append(line)
                line = reader.readLine()
            }
        } finally {
            reader.close()
        }
        return content.toString()
    }
}
```

This class:
- Stores the raw HTTP response
- Reads the response body into a string
- Optionally casts the response to a specific class for type-safe assertions

### 4️⃣Implementing Step Definitions

Step definitions connect the Gherkin language in feature files to executable Kotlin code:

```kotlin
class AuthSteps: CucumberBase() {
    @Before
    fun setup() {
        restTemplate = restTemplateBuilder.rootUri("http://localhost:$localPort/api/v1/auth").build()
    }

    @Then(value = "the client calls {string} with username {string} and password {string} and cast to {string}")
    fun theClientCallsWithCredentialsAndCastsToClass(url: String, username: String, password: String, classToCast: String) {
        executePost(
            url = url,
            entries = mutableMapOf("username" to username, "password" to password),
            classToCast = Class.forName(classToCast) as Class<*>
        )
    }

    @Then(value = "^the client receives status code of (\\d+)$")
    fun theClientReceivesStatusCode(statusCode: Int) {
        val currentStatusCode: HttpStatusCode = latestResponse.theResponse.statusCode
        assertEquals(expected = statusCode.toLong(), actual = currentStatusCode.value().toLong())
    }

    @Then(value = "response has field {string}")
    fun responseHasField(field: String) {
        val readTree = objectMapper.readTree(latestResponse.body)
        assertTrue { readTree.get(field) != null }
    }
}
```

Key aspects of step definitions:
- **Dynamic base URI** construction using injected port
- **`@Before` hook** configures the test environment
- **Parameterized step patterns** with regex capture groups
- **Method implementation** that executes HTTP requests
- **Assertions** validate response codes and content
- **Type-safe casting** for response objects

### 5️⃣Test Execution Flow Visualization
Here's a diagram illustrating the flow of test execution:

```
┌────────────────┐     ┌─────────────────┐     ┌───────────────────┐
│                │     │                 │     │                   │
│  Auth.feature  ├────▶│  AuthSteps.kt   ├────▶│   CucumberBase    │
│                │     │                 │     │                   │
└────────────────┘     └─────────────────┘     └─────────┬─────────┘
                                                         │
┌────────────────┐     ┌─────────────────┐     ┌─────────▼─────────┐
│                │     │                 │     │                   │
│ Spring Context │◀────┤ PostgreSQL      │◀────┤   REST Template   │
│                │     │ (Testcontainers)│     │                   │
└────────────────┘     └─────────────────┘     └───────────────────┘
```

#### 🔧Gradle Setup for Testing
The `build.gradle.kts` file includes specific configurations for running Cucumber tests:

```kotlin
dependencies {
    // Other dependencies...
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("io.cucumber:cucumber-java:7.23.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.23.0")
    testImplementation("io.cucumber:cucumber-spring:7.23.0")
    testImplementation("org.testcontainers:testcontainers:1.21.3")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testImplementation("org.testcontainers:postgresql:1.21.3")
}

tasks.withType<Test> {
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    // ...
}

tasks.register<Test>(name = "integrationTest") {
    description = "Runs the integration tests"
    group = "Verification"
    include("**/*IT.*")
    useJUnitPlatform()
}
```

Key configuration elements:
- **Cucumber dependencies** for BDD testing
- **JUnit platform** for integration
- **Testcontainers** for database isolation
- **Task configuration** for specific test types

#### 🧪Command Line Execution

```bash
# Run all tests including Cucumber
./gradlew test

# Run only integration tests
./gradlew integrationTest

# Skip specific test types
./gradlew test -PskipTests=integration
./gradlew test -Dspring.profiles.active=cucumber-test

# Skip all tests  
./gradlew test -PskipTests=all
```

#### Debugging Tips

1. **View Cucumber output** with the `pretty` formatter:
   ```kotlin
   @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
   ```

2. **Inspect HTTP requests/responses** by adding debug logs:
   ```kotlin
   private val log: Logger by logger()
   log.debug("Request body: ${requestCallback.body}")
   ```

3. **Use explicit assertions** for clear failure messages:
   ```kotlin
   assertEquals(
       expected = statusCode.toLong(), 
       actual = currentStatusCode.value().toLong(),
       message = "Expected status ${statusCode} but got ${currentStatusCode.value()}"
   )
   ```

## 🔍Execution Flow

1. **Container Startup** - PostgreSQL container initializes
2. **Spring Context** - Application starts with random port  
3. **Feature Loading** - Gherkin files are parsed
4. **Step Execution** - HTTP requests hit live application
5. **Response Validation** - JSON responses are verified
6. **Cleanup** - Container and context shutdown

## 🎯Best Practices Demonstrated

### 1. **Separation of Concerns**
- Base class handles HTTP mechanics
- Step definitions focus on business logic  
- Feature files contain readable scenarios

### 2. **Reusable Components**
- Generic HTTP methods support multiple endpoints
- Parameterized steps reduce code duplication
- Response objects enable cross-step validation

### 3. **Robust Error Handling**
- Custom error handler captures failures
- Response details preserved for debugging
- Both success and error paths testable

### 4. **Database Isolation**
- Testcontainers provide clean database state
- No test pollution between scenarios
- Realistic database behavior

### 5. **Configuration Management**
- Profile-specific settings (cucumber-test)
- Flyway disabled for faster startup
- Environment-specific properties

## 🎉Benefits of This Architecture

1. **Maintainable Test Suite**
   - Adding new scenarios is straightforward
   - Step definitions are organized by domain
   - Common patterns are abstracted
2. **Clean Separation of Concerns**
    - Feature files describe business requirements
    - Step definitions implement test logic
    - Base class handles technical HTTP details
    - Scalable components
3. **Reliable**
   - Isolated database state prevents flaky tests
4. **Reusable Components**
    - HTTP client functionality is centralized
    - Common assertions are abstracted
    - Setup code is shared across scenarios
    - Fast feedback loop with Testcontainers
5. **Readable**
   - Business-friendly Gherkin scenarios
6. **Realistic Testing Environment**
    - Tests run against a full Spring context
    - Database interactions use real PostgreSQL
    - HTTP requests hit actual endpoints

## 🚦Conclusion
This Cucumber integration testing architecture provides a powerful and solid foundation for BDD-style integration testing in a Spring Boot. By combining the readability of Gherkin with the power of Spring's testing framework and the isolation of Testcontainers, you can build a comprehensive, maintainable test suite that validates your application's behavior from end to end.

The approach demonstrated here ensures that your tests:
- Are readable by all stakeholders
- Provide living documentation
- Test realistic scenarios
- Maintain isolation between test runs
- Can be extended to cover new features

By adopting these patterns, you can build confidence in your application's behavior while creating a valuable resource for understanding its requirements and functionality.

## 🖥️Demo
This guide demonstrates how to implement robust Behavior-Driven Development (BDD) integration tests using Cucumber in a Spring Boot. The setup leverages Testcontainers for database isolation and provides a clean, maintainable testing architecture.

> 📌 **GitHub Repository**: [https://github.com/senocak/Spring-Kotlin-Jwt-BDD](https://github.com/senocak/Spring-Kotlin-Jwt-BDD)

## 🔮What's Next?

Now that you have a solid foundation for Cucumber integration testing with Spring Boot, here are some ways to enhance your testing suite:

### 1. **Expand Test Coverage**
- Add feature files for other endpoints in your API
- Test edge cases and error scenarios
- Create scenarios for complex business workflows

### 2. **Advanced Cucumber Features**
- Use **Scenario Outlines** for data-driven testing:
  ```gherkin
  Scenario Outline: Login with various credentials
    When the client calls "/login" with username "<username>" and password "<password>"
    Then the client receives status code of <statusCode>
    
    Examples:
      | username      | password   | statusCode |
      | validUser     | validPass  | 200        |
      | invalidUser   | validPass  | 401        |
      | validUser     | wrongPass  | 401        |
  ```

- Add **Background** steps for common setup:
  ```gherkin
  Feature: User management
    
    Background:
      Given the database contains a user with username "admin"
      
    Scenario: Get user details
      When the client authenticates as "admin"
      And the client calls "/users/me"
      Then the client receives status code of 200
  ```

### 3. **Enhance Reporting**
- Add Cucumber HTML reports:
  ```kotlin
  @ConfigurationParameter(
      key = PLUGIN_PROPERTY_NAME, 
      value = "pretty, html:build/reports/cucumber/report.html"
  )
  ```

- Integrate with test reporting tools like Allure:
  ```kotlin
  dependencies {
      // existing dependencies...
      testImplementation("io.qameta.allure:allure-cucumber7-jvm:2.24.0")
  }
  ```

### 4. **Parallel Test Execution**
- Configure Cucumber for parallel execution:
  ```kotlin
  @ConfigurationParameter(
      key = "cucumber.execution.parallel.enabled",
      value = "true"
  )
  @ConfigurationParameter(
      key = "cucumber.execution.parallel.config.strategy",
      value = "dynamic"
  )
  ```

### 5. **CI/CD Integration**
- Add GitHub Actions workflow for automated testing:
  ```yaml
  name: Cucumber Tests
  
  on: [push, pull_request]
  
  jobs:
    test:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - name: Set up JDK 17
          uses: actions/setup-java@v3
          with:
            java-version: '17'
            distribution: 'temurin'
        - name: Run tests
          run: ./gradlew integrationTest
        - name: Publish Test Report
          uses: mikepenz/action-junit-report@v3
          if: always()
          with:
            report_paths: 'build/test-results/test/TEST-*.xml'
  ```

### 6. **Custom Step Libraries**
- Create domain-specific step libraries for different parts of your application:
  ```kotlin
  class UserManagementSteps: CucumberBase() {
      // User-specific step definitions
  }
  
  class PaymentSteps: CucumberBase() {
      // Payment-specific step definitions
  }
  ```

### 7. **Visual Testing Integration**
- Add screenshot capture for UI tests:
  ```kotlin
  @Then("take screenshot")
  fun takeScreenshot() {
      // Screenshot logic for Selenium or similar
  }
  ```

By implementing these enhancements, you'll create an even more robust and maintainable testing framework that scales with your application's complexity.

## 🔗Additional Resources

- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [Spring Boot Testing Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Testcontainers for Java](https://www.testcontainers.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
