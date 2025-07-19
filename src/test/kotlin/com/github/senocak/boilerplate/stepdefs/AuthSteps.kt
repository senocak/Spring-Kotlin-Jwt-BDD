package com.github.senocak.boilerplate.stepdefs

import io.cucumber.java.Before
import io.cucumber.java.en.Then
import org.springframework.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step definitions for authentication-related Cucumber tests.
 *
 * This class extends [CucumberBase] to provide reusable functionality and
 * configures the `RestTemplate` to point to the authentication API endpoint.
 *
 * It includes steps to perform POST requests with credentials, verify
 * HTTP status codes, and check for presence of fields in JSON responses.
 * @author tcasenocak
 */
class AuthSteps: CucumberBase() {

    /**
     * Setup method executed before each scenario.
     *
     * Initializes the [restTemplate] with the base URI for the authentication API,
     * using the dynamic [localPort] injected by Spring Boot.
     */
    @Before
    fun setup() {
        restTemplate = restTemplateBuilder.rootUri("http://localhost:$localPort/api/v1/auth").build()
    }

    /**
     * Performs a POST request to the given [url] with the specified [username] and [password]
     * as JSON body parameters, and casts the response to the class specified by [classToCast].
     *
     * This step is typically used for login or token request scenarios.
     *
     * @param url the endpoint path to call (relative to the base URI)
     * @param username the username to include in the request body
     * @param password the password to include in the request body
     * @param classToCast fully qualified name of the class to deserialize the response into
     */
    @Then(value = "the client calls {string} with username {string} and password {string} and cast to {string}")
    fun theClientCallsWithCredentialsAndCastsToClass(url: String, username: String, password: String, classToCast: String) {
        executePost(
            url = url,
            entries = mutableMapOf("username" to username, "password" to password),
            classToCast = Class.forName(classToCast) as Class<*>
        )
    }

    /**
     * Verifies that the HTTP status code received in the latest response
     * matches the expected [statusCode].
     *
     * @param statusCode the expected HTTP status code
     */
    @Then(value = "^the client receives status code of (\\d+)$")
    fun theClientReceivesStatusCode(statusCode: Int) {
        val currentStatusCode: HttpStatusCode = latestResponse.theResponse.statusCode
        assertEquals(expected = statusCode.toLong(), actual = currentStatusCode.value().toLong())
    }

    /**
     * Asserts that the latest JSON response contains a field named [field].
     *
     * This step parses the response body and checks for the existence
     * of the given top-level JSON property.
     *
     * @param field the name of the JSON field expected in the response
     */
    @Then(value = "response has field {string}")
    fun responseHasField(field: String) {
        val readTree = objectMapper.readTree(latestResponse.body)
        assertTrue { readTree.get(field) != null }
    }
}
