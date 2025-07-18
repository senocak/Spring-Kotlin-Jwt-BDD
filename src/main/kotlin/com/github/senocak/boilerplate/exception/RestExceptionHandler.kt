package com.github.senocak.boilerplate.exception

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.github.senocak.boilerplate.domain.dto.ExceptionDto
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import com.github.senocak.boilerplate.util.logger
import org.slf4j.Logger
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import java.lang.reflect.UndeclaredThrowableException
import java.security.InvalidParameterException
import jakarta.validation.ConstraintViolationException
import kotlin.getValue

@RestControllerAdvice
class RestExceptionHandler {
    private val log: Logger by logger()

    @ExceptionHandler(value = [
        BadCredentialsException::class,
        ConstraintViolationException::class,
        InvalidParameterException::class,
        TypeMismatchException::class,
        MissingPathVariableException::class,
        HttpMessageNotReadableException::class,
        MissingServletRequestParameterException::class,
        MismatchedInputException::class,
        UndeclaredThrowableException::class
    ])
    fun handleBadRequestException(ex: Exception): ResponseEntity<Any> =
        generateResponseEntity(httpStatus = HttpStatus.BAD_REQUEST,
            omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT, variables = arrayOf(ex.message))

    @ExceptionHandler(value = [
        AccessDeniedException::class,
        AuthenticationCredentialsNotFoundException::class,
        UnrecognizedPropertyException::class
    ])
    fun handleUnAuthorized(ex: Exception): ResponseEntity<Any> =
        generateResponseEntity(httpStatus = HttpStatus.UNAUTHORIZED,
            omaErrorMessageType = OmaErrorMessageType.UNAUTHORIZED, variables = arrayOf(ex.message))

    @ExceptionHandler(value = [HttpRequestMethodNotSupportedException::class])
    fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<Any> =
        generateResponseEntity(httpStatus = HttpStatus.METHOD_NOT_ALLOWED,
            omaErrorMessageType = OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED, variables = arrayOf(ex.message))

    @ExceptionHandler(value = [HttpMediaTypeNotSupportedException::class])
    fun handleHttpMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<Any> =
        generateResponseEntity(httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT, variables = arrayOf(ex.message))

    @ExceptionHandler(value = [NoHandlerFoundException::class, UsernameNotFoundException::class])
    fun handleNoHandlerFoundException(ex: Exception): ResponseEntity<Any> =
        generateResponseEntity(httpStatus = HttpStatus.NOT_FOUND,
            omaErrorMessageType = OmaErrorMessageType.NOT_FOUND, variables = arrayOf(ex.message))

    @ExceptionHandler(value = [ServerException::class])
    fun handleServerException(ex: ServerException): ResponseEntity<Any> =
        generateResponseEntity(httpStatus = ex.statusCode, omaErrorMessageType = ex.omaErrorMessageType,
            variables = ex.variables)

    @ExceptionHandler(value = [Exception::class])
    fun handleGeneralException(ex: Exception): ResponseEntity<Any> =
        generateResponseEntity(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR, variables = arrayOf(ex.message))

    /**
     * @param httpStatus -- returned code
     * @return -- returned body
     */
    private fun generateResponseEntity(
        httpStatus: HttpStatus,
        omaErrorMessageType: OmaErrorMessageType,
        variables: Array<String?>
    ): ResponseEntity<Any> {
        log.error("Exception is handled. HttpStatus: $httpStatus, OmaErrorMessageType: $omaErrorMessageType, variables: $variables")
        val exceptionDto = ExceptionDto()
        exceptionDto.statusCode = httpStatus.value()
        exceptionDto.error = ExceptionDto.OmaErrorMessageTypeDto(
            id = omaErrorMessageType.messageId,
            text = omaErrorMessageType.text
        )
        exceptionDto.variables = variables
        return ResponseEntity.status(httpStatus).body(exceptionDto)
    }
}
