package com.github.senocak.boilerplate.controller

import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.CrossOrigin

@CrossOrigin(origins = ["*"], maxAge = 3600)
abstract class BaseController {
    fun validate(resultOfValidation: BindingResult) {
        if (resultOfValidation.hasErrors()) {
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                variables = resultOfValidation.fieldErrors
                    .map { fieldError: FieldError? -> "${fieldError?.field}: ${fieldError?.defaultMessage}" }
                    .toList()
                    .toTypedArray(),
                statusCode = HttpStatus.BAD_REQUEST)
        }
    }
}
