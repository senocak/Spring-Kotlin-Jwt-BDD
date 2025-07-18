package com.github.senocak.boilerplate.exception

import com.github.senocak.boilerplate.domain.dto.ExceptionDto
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.servlet.NoHandlerFoundException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Tag(value = "unit")
@ExtendWith(value = [MockitoExtension::class])
@DisplayName(value = "Unit Tests for RestExceptionHandler")
class RestExceptionHandlerTest {
    private val restExceptionHandler: RestExceptionHandler = RestExceptionHandler()

    @Test
    fun givenExceptionWhenHandleBadRequestExceptionThenAssertResult() {
        // Given
        val ex: Exception = BadCredentialsException("lorem")
        // When
        val handleBadRequestException: ResponseEntity<Any> = restExceptionHandler.handleBadRequestException(ex = ex)
        val exceptionDto: ExceptionDto? = handleBadRequestException.body as ExceptionDto?
        // Then
        assertNotNull(actual = exceptionDto)
        assertEquals(expected = HttpStatus.BAD_REQUEST, actual = handleBadRequestException.statusCode)
        assertEquals(expected = HttpStatus.BAD_REQUEST.value(), actual = exceptionDto.statusCode)
        assertEquals(expected = OmaErrorMessageType.BASIC_INVALID_INPUT.messageId, actual = exceptionDto.error!!.id)
        assertEquals(expected = OmaErrorMessageType.BASIC_INVALID_INPUT.text, actual = exceptionDto.error!!.text)
        assertEquals(expected = 1, actual = exceptionDto.variables.size)
        assertEquals(expected = ex.message, actual = exceptionDto.variables.first())
    }

    @Test
    fun givenExceptionWhenHandleUnAuthorizedThenAssertResult() {
        // Given
        val ex: RuntimeException = AccessDeniedException("lorem")
        // When
        val handleBadRequestException: ResponseEntity<Any> = restExceptionHandler.handleUnAuthorized(ex = ex)
        val exceptionDto: ExceptionDto? = handleBadRequestException.body as ExceptionDto?
        // Then
        assertNotNull(actual = exceptionDto)
        assertEquals(expected = HttpStatus.UNAUTHORIZED, actual = handleBadRequestException.statusCode)
        assertEquals(expected = HttpStatus.UNAUTHORIZED.value(), actual = exceptionDto.statusCode)
        assertEquals(expected = OmaErrorMessageType.UNAUTHORIZED.messageId, actual = exceptionDto.error!!.id)
        assertEquals(expected = OmaErrorMessageType.UNAUTHORIZED.text, actual = exceptionDto.error!!.text)
        assertEquals(expected = 1, actual = exceptionDto.variables.size)
        assertEquals(expected = ex.message, actual = exceptionDto.variables.first())
    }

    @Test
    fun givenExceptionWhenHandleServerExceptionThenAssertResult() {
        // Given
        val errrMsg = "lorem"
        val ex = ServerException(omaErrorMessageType = OmaErrorMessageType.NOT_FOUND,
            variables = arrayOf(errrMsg), statusCode = HttpStatus.CONFLICT)
        // When
        val handleBadRequestException: ResponseEntity<Any> = restExceptionHandler.handleServerException(ex = ex)
        val exceptionDto: ExceptionDto? = handleBadRequestException.body as ExceptionDto?
        // Then
        assertNotNull(actual = exceptionDto)
        assertEquals(expected = HttpStatus.CONFLICT, actual = handleBadRequestException.statusCode)
        assertEquals(expected = HttpStatus.CONFLICT.value(), actual = exceptionDto.statusCode)
        assertEquals(expected = OmaErrorMessageType.NOT_FOUND.messageId, actual = exceptionDto.error!!.id)
        assertEquals(expected = OmaErrorMessageType.NOT_FOUND.text, actual = exceptionDto.error!!.text)
        assertEquals(expected = 1, actual = exceptionDto.variables.size)
        assertEquals(expected = errrMsg, actual = exceptionDto.variables.first())
    }

    @Test
    fun givenExceptionWhenHandleGeneralExceptionThenAssertResult() {
        // Given
        val ex = Exception("lorem")
        // When
        val handleBadRequestException: ResponseEntity<Any> = restExceptionHandler.handleGeneralException(ex = ex)
        val exceptionDto: ExceptionDto? = handleBadRequestException.body as ExceptionDto?
        // Then
        assertNotNull(actual = exceptionDto)
        assertEquals(expected = HttpStatus.INTERNAL_SERVER_ERROR, actual = handleBadRequestException.statusCode)
        assertEquals(expected = HttpStatus.INTERNAL_SERVER_ERROR.value(), actual = exceptionDto.statusCode)
        assertEquals(expected = OmaErrorMessageType.GENERIC_SERVICE_ERROR.messageId, actual = exceptionDto.error!!.id)
        assertEquals(expected = OmaErrorMessageType.GENERIC_SERVICE_ERROR.text, actual = exceptionDto.error!!.text)
        assertEquals(expected = 1, actual = exceptionDto.variables.size)
        assertEquals(expected = ex.message, actual = exceptionDto.variables.first())
    }

    @Test
    fun givenExceptionWhenHandleHttpRequestMethodNotSupportedThenAssertResult() {
        // Given
        val ex = HttpRequestMethodNotSupportedException("lorem")
        // When
        val handleBadRequestException: ResponseEntity<Any> = restExceptionHandler.handleHttpRequestMethodNotSupported(ex = ex)
        val exceptionDto: ExceptionDto? = handleBadRequestException.body as ExceptionDto?
        // Then
        assertNotNull(actual = exceptionDto)
        assertEquals(expected = HttpStatus.METHOD_NOT_ALLOWED, actual = handleBadRequestException.statusCode)
        assertEquals(expected = HttpStatus.METHOD_NOT_ALLOWED.value(), actual = exceptionDto.statusCode)
        assertEquals(expected = OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED.messageId, actual = exceptionDto.error!!.id)
        assertEquals(expected = OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED.text, actual = exceptionDto.error!!.text)
        assertEquals(expected = 1, actual = exceptionDto.variables.size)
        assertEquals(expected = ex.message, actual = exceptionDto.variables.first())
    }

    @Test
    fun givenExceptionWhenHandleHttpMediaTypeNotSupportedThenAssertResult() {
        // Given
        val ex = HttpMediaTypeNotSupportedException("lorem")
        // When
        val handleBadRequestException: ResponseEntity<Any> = restExceptionHandler.handleHttpMediaTypeNotSupported(ex = ex)
        val exceptionDto: ExceptionDto? = handleBadRequestException.body as ExceptionDto?
        // Then
        assertNotNull(actual = exceptionDto)
        assertEquals(expected = HttpStatus.UNSUPPORTED_MEDIA_TYPE, actual = handleBadRequestException.statusCode)
        assertEquals(expected = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), actual = exceptionDto.statusCode)
        assertEquals(expected = OmaErrorMessageType.BASIC_INVALID_INPUT.messageId, actual = exceptionDto.error!!.id)
        assertEquals(expected = OmaErrorMessageType.BASIC_INVALID_INPUT.text, actual = exceptionDto.error!!.text)
        assertEquals(expected = 1, actual = exceptionDto.variables.size)
        assertEquals(expected = ex.message, actual = exceptionDto.variables.first())
    }

    @Test
    fun givenExceptionWhenHandleNoHandlerFoundExceptionThenAssertResult() {
        // Given
        val ex = NoHandlerFoundException("GET", "", HttpHeaders())
        // When
        val handleBadRequestException: ResponseEntity<Any> = restExceptionHandler.handleNoHandlerFoundException(ex = ex)
        val exceptionDto: ExceptionDto? = handleBadRequestException.body as ExceptionDto?
        // Then
        assertNotNull(actual = exceptionDto)
        assertEquals(expected = HttpStatus.NOT_FOUND, actual = handleBadRequestException.statusCode)
        assertEquals(expected = HttpStatus.NOT_FOUND.value(), actual = exceptionDto.statusCode)
        assertEquals(expected = OmaErrorMessageType.NOT_FOUND.messageId, actual = exceptionDto.error!!.id)
        assertEquals(expected = OmaErrorMessageType.NOT_FOUND.text, actual = exceptionDto.error!!.text)
        assertEquals(expected = 1, actual = exceptionDto.variables.size)
        assertEquals(expected = "No endpoint GET .", actual = exceptionDto.variables.first())
    }
}
