# Cucumber Integration Testing in Spring Boot Kotlin JWT Application

This guide demonstrates how to implement robust Behavior-Driven Development (BDD) integration tests using Cucumber in a Spring Boot Kotlin application with JWT authentication. The setup leverages Testcontainers for database isolation and provides a clean, maintainable testing architecture.

## 🎯 Why Cucumber for Integration Testing?

Cucumber enables writing human-readable feature specifications that directly map to automated tests. This approach:

- **Bridges the gap** between technical and non-technical stakeholders
- **Ensures requirements** are clear and testable
- **Provides living documentation** that stays in sync with code
- **Enables BDD workflows** for better collaboration

## 🏗️ Project Structure

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

## 🔧 Configuration Deep Dive

### Test Configuration (`CucumberTestConfig.kt`)

The configuration uses several key annotations:

- `@CucumberContextConfiguration` - Integrates Cucumber with Spring Boot
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` - Starts embedded server
- `@ContextConfiguration(initializers = [PostgresqlInitializer::class])` - Database setup
- `@ActiveProfiles("cucumber-test")` - Uses test-specific configuration
- `@Transactional(propagation = NOT_SUPPORTED)` - Prevents transaction rollback

### Database Setup with Testcontainers

The `PostgresqlInitializer` configures a containerized PostgreSQL instance:

```kotlin
@ContextConfiguration(initializers = [PostgresqlInitializer::class])
```

This ensures each test run uses a fresh, isolated database instance.

## 📝 Writing Feature Files

Feature files are written in Gherkin syntax and stored in `src/test/resources/features/`:

```gherkin
Feature: The health of the application can be checked
  Scenario: client makes call to POST to login
    When the client calls "/login" with username "asenocakAdmin" and password "asenocak" and cast to "com.github.senocak.boilerplate.domain.dto.UserWrapperResponse"
    Then the client receives status code of 200
    Then response has field "token"
```

### Key Benefits of This Approach:
- **Readable scenarios** that non-technical team members can understand
- **Parameterized steps** for flexible test data
- **Response validation** with type casting
- **Field-level assertions** on JSON responses

## 🔨 Step Definitions Architecture

### Base Class (`CucumberBase.kt`)

Provides reusable HTTP client functionality:

```kotlin
@CucumberTestConfig
open class CucumberBase {
    @LocalServerPort protected var localPort: Int = 0
    @Autowired protected lateinit var objectMapper: ObjectMapper
    @Autowired protected lateinit var restTemplateBuilder: RestTemplateBuilder
    
    protected lateinit var restTemplate: RestTemplate
    protected lateinit var latestResponse: ResponseResults
}
```

**Key Features:**
- **Dynamic port injection** with `@LocalServerPort`
- **Jackson integration** for JSON parsing
- **RestTemplate configuration** for HTTP requests
- **Response caching** for step-to-step data sharing
- **Custom error handling** with `ResponseResultErrorHandler`

### HTTP Request Methods

The base class provides methods for all HTTP verbs:

- `executeGet(url: String)` - GET requests
- `executePost(url, entries, classToCast)` - POST with JSON body
- `executePut(url, entries)` - PUT requests
- `executeDelete(url)` - DELETE requests

### Custom Error Handling

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

## 🔐 Authentication Step Implementation

### Step Definitions (`AuthSteps.kt`)

```kotlin
class AuthSteps: CucumberBase() {
    @Before
    fun setup() {
        restTemplate = restTemplateBuilder
            .rootUri("http://localhost:$localPort/api/v1/auth")
            .build()
    }
    
    @Then("the client calls {string} with username {string} and password {string} and cast to {string}")
    fun theClientCallsWithCredentialsAndCastsToClass(
        url: String, 
        username: String, 
        password: String, 
        classToCast: String
    ) {
        executePost(
            url = url,
            entries = mutableMapOf("username" to username, "password" to password),
            classToCast = Class.forName(classToCast) as Class<*>
        )
    }
}
```

**Implementation Highlights:**
- **Dynamic base URI** construction using injected port
- **Flexible credentials** passed as step parameters  
- **Type-safe response casting** using reflection
- **Reusable HTTP logic** from base class

## 🚀 Running the Tests

### Execute All Tests
```bash
./gradlew test
```

### Run Only Integration Tests  
```bash
./gradlew integrationTest
```

### Run with Specific Profiles
```bash
./gradlew test -Dspring.profiles.active=cucumber-test
```

### Skip Specific Test Types
```bash
# Skip integration tests only
./gradlew test -PskipTests=integration

# Skip all tests  
./gradlew test -PskipTests=all
```

## 📊 Test Execution Flow

1. **Container Startup** - PostgreSQL container initializes
2. **Spring Context** - Application starts with random port  
3. **Feature Loading** - Gherkin files are parsed
4. **Step Execution** - HTTP requests hit live application
5. **Response Validation** - JSON responses are verified
6. **Cleanup** - Container and context shutdown

## 🎯 Best Practices Demonstrated

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

## 🔍 Advanced Features

### Response Validation Chain
```gherkin
When the client calls "/login" with credentials
Then the client receives status code of 200  
And response has field "token"
And response has field "user.username"
```

### Custom Request Headers
```kotlin
val headers: MutableMap<String, String> = hashMapOf(
    "Accept" to "application/json",
    "Content-Type" to "application/json"
)
```

### Type-Safe Response Casting
```kotlin
executePost(
    url = "/login",
    entries = credentials,
    classToCast = UserWrapperResponse::class.java
)
```

## 🎉 Benefits of This Architecture

- **Maintainable** - Clean separation between test layers
- **Scalable** - Easy to add new endpoints and scenarios  
- **Reliable** - Isolated database state prevents flaky tests
- **Fast** - Optimized Spring context reuse
- **Readable** - Business-friendly Gherkin scenarios
- **Comprehensive** - Tests full request/response cycle

## 🚦 Getting Started

1. **Add Dependencies** - Cucumber, Spring Boot Test, Testcontainers
2. **Configure Annotation** - Use `@CucumberTestConfig`  
3. **Write Features** - Create `.feature` files in `src/test/resources/features/`
4. **Implement Steps** - Extend `CucumberBase` for HTTP utilities
5. **Run Tests** - Execute via Gradle or IDE

This setup provides a solid foundation for BDD-style integration testing in Spring Boot Kotlin applications, ensuring your authentication flows and API contracts are thoroughly validated with maintainable, readable tests.
