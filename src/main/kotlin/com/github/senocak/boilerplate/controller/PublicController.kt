package com.github.senocak.boilerplate.controller

import com.github.senocak.boilerplate.domain.dto.ExceptionDto
import com.github.senocak.boilerplate.domain.dto.UserWrapperResponse
import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.util.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = [PublicController.URL])
@Tag(name = "Public", description = "Public Controller")
class PublicController: BaseController() {
    private val log: Logger by logger()

    @Throws(exceptionClasses = [ServerException::class])
    @Operation(summary = "Ping", tags = ["Public"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UserWrapperResponse::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    @GetMapping(value = ["/ping"])
    fun me(): String {
        return "ping"
    }

    companion object {
        const val URL = "/api/v1/public"
    }
}
