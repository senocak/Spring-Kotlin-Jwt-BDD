package com.github.senocak.boilerplate.stepdefs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.boilerplate.config.CucumberTestConfig
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.URI

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

    /**
     * Executes an HTTP GET request to the specified [url].
     * Applies configured headers and uses a custom error handler to capture error responses.
     * The response (successful or error) is saved in [latestResponse].
     *
     * @param url the endpoint URL to send the GET request to
     * @throws IOException if the response is null or an error occurs during the request execution
     */
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

    /**
     * Executes an HTTP DELETE request to the specified [url].
     * Uses the configured headers and a custom error handler to capture any error responses.
     * The response (successful or error) is stored in [latestResponse].
     *
     * @param url the endpoint URL to send the DELETE request to
     * @throws IOException if the response is null or an error occurs during the request execution
     */
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

    /**
     * Executes an HTTP PUT request to the specified [url] with an optional JSON-formatted body
     * constructed from [entries].
     *
     * If [entries] is not empty, it will be serialized to JSON and set as the request body.
     * A custom error handler is used to capture any error responses.
     *
     * The response (successful or error) is stored in [latestResponse].
     *
     * @param url the endpoint URL to send the PUT request to
     * @param entries a mutable map of key-value pairs to be serialized into JSON as the request body; can be empty
     *
     * @throws IOException if the response is null or an error occurs during the request execution
     */
    @Throws(exceptionClasses = [IOException::class])
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

    /**
     * Executes an HTTP POST request to the specified [url] with JSON-formatted [entries] as the body,
     * and attempts to cast the response to the given [classToCast].
     *
     * Sets the "Content-Type" header to "application/json", prepares the request with headers and body,
     * and sets a custom error handler to capture error responses.
     *
     * The response is stored in [latestResponse], which contains either the successful response
     * or error details if the request failed.
     *
     * @param url the endpoint URL to send the POST request to
     * @param entries a mutable map of key-value pairs to be serialized into JSON as the request body
     * @param classToCast the class type to cast the response body into for deserialization
     *
     * @throws IOException if the response is null or an error occurs during the request execution
     */
    @Throws(exceptionClasses = [IOException::class])
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

/**
 * A [ResponseErrorHandler] implementation that detects server errors (HTTP 5xx)
 * and captures the response details for further inspection.
 *
 * @property results Stores the [ResponseResults] of the last error response handled.
 * @property hadError Indicates whether the last response had a server error (status code >= 500).
 *
 * This error handler marks responses with status codes 500 and above as errors
 * and saves the response details when such errors occur.
 * @author tcasenocak
 */
class ResponseResultErrorHandler: ResponseErrorHandler {
    var results: ResponseResults? = null
        private set
    var hadError = false

    /**
     * Determines whether the given HTTP response has an error status.
     *
     * This implementation considers responses with status code 500 or higher
     * as errors.
     *
     * @param response the HTTP response to check
     * @return `true` if the response status code is 500 or greater, `false` otherwise
     * @throws IOException if an I/O error occurs while accessing the response
     */
    @Throws(exceptionClasses = [IOException::class])
    override fun hasError(response: ClientHttpResponse): Boolean {
        hadError = response.statusCode.value() >= 500
        return hadError
    }

    /**
     * Handles an error response by capturing its details into [ResponseResults].
     *
     * This method is called when an error response is detected, allowing
     * the response to be stored for later inspection or assertions.
     *
     * @param url the URI of the request that caused the error
     * @param method the HTTP method of the request
     * @param response the error [ClientHttpResponse] received
     * @throws IOException if an I/O error occurs while reading the response
     */
    @Throws(exceptionClasses = [IOException::class])
    override fun handleError(url: URI, method: HttpMethod, response: ClientHttpResponse) {
        results = ResponseResults(theResponse = response)
    }
}

/**
 * Holds the HTTP response details for test assertions or further processing.
 *
 * @property theResponse The raw [ClientHttpResponse] received from the client.
 * @property classToCast Optional class reference to cast the response body into a specific type.
 * @property body The full response body as a plain string, read eagerly during construction.
 *
 * This class is primarily used in integration or BDD-style tests to capture the
 * response and inspect its contents (e.g., status code, body, headers).
 * @author tcasenocak
 */
class ResponseResults internal constructor(
    val theResponse: ClientHttpResponse,
    val classToCast: Class<*>? = null,
) {
    var body: String = toString(inputStream = theResponse.body)

    /**
     * Reads the entire content of the given [InputStream] and returns it as a [String].
     *
     * This function reads the input stream line by line using a [BufferedReader]
     * and appends each line to a [StringBuilder]. The reader is closed after reading.
     *
     * @param inputStream the input stream to read from
     * @return the full content of the input stream as a string
     * @throws IOException if an I/O error occurs while reading the stream
     */
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
 * A [RequestCallback] implementation used to set custom headers and an optional body
 * on an outgoing HTTP request.
 *
 * @constructor Creates a callback with the provided headers to be added to the request.
 * @param requestHeaders A mutable map of headers to add to the request.
 *
 * This class is typically used with Spring's [RestTemplate] for programmatically configuring
 * request headers and body content before executing a request.
 * @author tcasenocak
 */
class HeaderSettingRequestCallback internal constructor(private val requestHeaders: MutableMap<String, String>): RequestCallback {
    private var body: String? = null

    /**
     * Sets the request body content to be written during the HTTP request.
     *
     * @param postBody the string content to set as the request body
     */
    fun setBody(postBody: String) {
        this.body = postBody
    }

    /**
     * Modifies the given [ClientHttpRequest] by adding headers and optionally writing a body.
     *
     * - Adds all entries from [requestHeaders] to the request's headers.
     * - If [body] is set, writes its bytes to the request body output stream.
     *
     * @param request the HTTP request to modify before execution
     * @throws IOException if an I/O error occurs while writing the body
     */
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
