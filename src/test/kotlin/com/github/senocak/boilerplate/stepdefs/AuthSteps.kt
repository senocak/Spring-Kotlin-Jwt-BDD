package com.github.senocak.boilerplate.stepdefs

import com.github.senocak.boilerplate.config.CucumberBase
import io.cucumber.java.Before
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration test checking if the health Check endpoint is working and returns the proper response
 *
 * @author tcasenocak
 */
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

