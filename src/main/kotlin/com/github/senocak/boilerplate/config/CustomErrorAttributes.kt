package com.github.senocak.boilerplate.config

import com.github.senocak.boilerplate.domain.dto.ExceptionDto
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import com.github.senocak.boilerplate.util.logger
import jakarta.servlet.RequestDispatcher
import org.slf4j.Logger
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest
import kotlin.getValue

@Configuration
@Profile("!integration-test")
class CustomErrorAttributes : DefaultErrorAttributes() {
    private val log: Logger by logger()

    override fun getErrorAttributes(webRequest: WebRequest, options: ErrorAttributeOptions): Map<String, Any> {
        val errorAttributes = super.getErrorAttributes(webRequest, options)
        val errorMessage = webRequest.getAttribute(RequestDispatcher.ERROR_MESSAGE, RequestAttributes.SCOPE_REQUEST)
        val exceptionDto = ExceptionDto()
        if (errorMessage != null) {
            val omaErrorMessageType = OmaErrorMessageType.NOT_FOUND
            exceptionDto.statusCode = errorAttributes["status"] as Int
            exceptionDto.variables = arrayOf(errorAttributes["error"].toString(), errorAttributes["message"].toString())
            exceptionDto.error = ExceptionDto.OmaErrorMessageTypeDto(omaErrorMessageType.messageId, omaErrorMessageType.text)
        }
        val map: MutableMap<String, Any> = HashMap()
        map["exception"] = exceptionDto
        log.debug("Exception occurred in DefaultErrorAttributes: $map")
        return map
    }
}
