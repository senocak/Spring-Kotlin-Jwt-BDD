package com.github.senocak.boilerplate.exception

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
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

    @ExceptionHandler(
        BadCredentialsException::class,
        ConstraintViolationException::class,
        InvalidParameterException::class,
        TypeMismatchException::class,
        MissingPathVariableException::class,
        HttpMessageNotReadableException::class,
        MissingServletRequestParameterException::class,
        MissingKotlinParameterException::class,
        UndeclaredThrowableException::class
    )
    fun handleBadRequestException(ex: Exception): ResponseEntity<Any> {
        return generateResponseEntity(
            HttpStatus.BAD_REQUEST,
            OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(ex.message)
        )
    }

    @ExceptionHandler(
        AccessDeniedException::class,
        AuthenticationCredentialsNotFoundException::class,
        UnrecognizedPropertyException::class
    )
    fun handleUnAuthorized(ex: Exception): ResponseEntity<Any> {
        return generateResponseEntity(
            HttpStatus.UNAUTHORIZED,
            OmaErrorMessageType.UNAUTHORIZED, arrayOf(ex.message)
        )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<Any> {
        return generateResponseEntity(
            HttpStatus.METHOD_NOT_ALLOWED,
            OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED, arrayOf(ex.message)
        )
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<Any> {
        return generateResponseEntity(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(ex.message)
        )
    }

    @ExceptionHandler(NoHandlerFoundException::class, UsernameNotFoundException::class)
    fun handleNoHandlerFoundException(ex: Exception): ResponseEntity<Any> {
        return generateResponseEntity(
            HttpStatus.NOT_FOUND,
            OmaErrorMessageType.NOT_FOUND, arrayOf(ex.message)
        )
    }

    @ExceptionHandler(ServerException::class)
    fun handleServerException(ex: ServerException): ResponseEntity<Any> {
        return generateResponseEntity(ex.statusCode, ex.omaErrorMessageType, ex.variables)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<Any> {
        return generateResponseEntity(
            HttpStatus.INTERNAL_SERVER_ERROR,
            OmaErrorMessageType.GENERIC_SERVICE_ERROR, arrayOf(ex.message)
        )
    }

    /**
     * @param httpStatus -- returned code
     * @return -- returned body
     */
    private fun generateResponseEntity(
        httpStatus: HttpStatus,
        omaErrorMessageType: OmaErrorMessageType,
        variables: Array<String?>
    ): ResponseEntity<Any> {
        log.error("Exception is handled. HttpStatus: {}, OmaErrorMessageType: {}, variables: {}",
            httpStatus, omaErrorMessageType, variables)
        val exceptionDto = ExceptionDto()
        exceptionDto.statusCode = httpStatus.value()
        exceptionDto.error = ExceptionDto.OmaErrorMessageTypeDto(
            omaErrorMessageType.messageId,
            omaErrorMessageType.text
        )
        exceptionDto.variables = variables
        return ResponseEntity.status(httpStatus).body(exceptionDto)
    }
}
