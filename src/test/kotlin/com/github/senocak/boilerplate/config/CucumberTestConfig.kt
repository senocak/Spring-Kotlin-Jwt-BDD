package com.github.senocak.boilerplate.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.boilerplate.config.initializer.PostgresqlInitializer
import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME
import io.cucumber.spring.CucumberContextConfiguration
import org.json.JSONObject
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestClassOrder
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.URI

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

/**
 * Test suite runner for the cucumber tests runs all the cucumber features defines under
 * the location specified in the cucumber options
 *
 * @author tcasenocak
 */
@CucumberTestConfig
open class CucumberBase {
    @LocalServerPort protected var localPort: Int = 0
    @Autowired protected lateinit var objectMapper: ObjectMapper
    @Autowired protected lateinit var restTemplateBuilder: RestTemplateBuilder
    protected lateinit var restTemplate: RestTemplate
    protected lateinit var latestResponse: ResponseResults
    val headers: MutableMap<String, String> = hashMapOf("Accept" to "application/json")

    @Throws(exceptionClasses = [IOException::class])
    fun executeGet(url: String) {
        val requestCallback: HeaderSettingRequestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
        val errorHandler = ResponseResultErrorHandler()
        restTemplate.setErrorHandler(errorHandler)
        latestResponse = restTemplate.execute(url, HttpMethod.GET, requestCallback,
            { response: ClientHttpResponse ->
                if (errorHandler.hadError) errorHandler.results else ResponseResults(theResponse = response)
            }) ?: throw IOException("Response was null for URL: $url")
    }

    @Throws(exceptionClasses = [IOException::class])
    fun executeDelete(url: String) {
        val requestCallback: HeaderSettingRequestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
        val errorHandler = ResponseResultErrorHandler()
        restTemplate.setErrorHandler(errorHandler)
        latestResponse = restTemplate.execute(url, HttpMethod.DELETE, requestCallback,
            { response: ClientHttpResponse? ->
                if (errorHandler.hadError) errorHandler.results else ResponseResults(theResponse = response!!)
            }) ?: throw IOException("Response was null for URL: $url")
    }

    @Throws(IOException::class)
    fun executePut(url: String, entries: MutableMap<String, Any>) {
        val requestCallback: HeaderSettingRequestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
        if (entries.isNotEmpty()) {
            requestCallback.setBody(JSONObject(entries).toString())
        }
        val errorHandler = ResponseResultErrorHandler()
        restTemplate.setErrorHandler(errorHandler)
        latestResponse = restTemplate.execute(url, HttpMethod.PUT, requestCallback,
            { response: ClientHttpResponse? ->
                if (errorHandler.hadError) errorHandler.results else ResponseResults(theResponse = response!!)
            }) ?: throw IOException("Response was null for URL: $url")
    }

    fun executePost(url: String, entries: MutableMap<String, Any>, classToCast: Class<*>) {
        headers.put("Content-Type", "application/json")
        val requestCallback: HeaderSettingRequestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
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
}

class ResponseResultErrorHandler: ResponseErrorHandler {
    var results: ResponseResults? = null
        private set
    var hadError = false

    @Throws(exceptionClasses = [IOException::class])
    override fun hasError(response: ClientHttpResponse): Boolean {
        hadError = response.statusCode.value() >= 500
        return hadError
    }

    @Throws(exceptionClasses = [IOException::class])
    override fun handleError(url: URI, method: HttpMethod, response: ClientHttpResponse) {
        results = ResponseResults(theResponse = response)
    }
}

/**
 * Takes in the http response and parses the body
 *
 * @author tcasenocak
 */
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

/**
 * Decorator of RequestCallback wraps the request callback and add to it a given body and header
 *
 * @author tcasenocak
 */
class HeaderSettingRequestCallback internal constructor(private val requestHeaders: MutableMap<String, String>): RequestCallback {
    private var body: String? = null

    fun setBody(postBody: String) {
        this.body = postBody
    }

    @Throws(exceptionClasses = [IOException::class])
    override fun doWithRequest(request: ClientHttpRequest) {
        val clientHeaders = request.headers
        requestHeaders.entries.forEach { entry: MutableMap.MutableEntry<String, String> ->
            clientHeaders.add(entry.key, entry.value)
        }
        if (body != null) {
            request.body.write(body!!.toByteArray())
        }
    }
}
