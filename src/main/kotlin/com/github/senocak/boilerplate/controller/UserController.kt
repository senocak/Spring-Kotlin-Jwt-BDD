package com.github.senocak.boilerplate.controller

import com.github.senocak.boilerplate.domain.dto.ExceptionDto
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.domain.dto.UpdateUserDto
import com.github.senocak.boilerplate.domain.dto.UserResponse
import com.github.senocak.boilerplate.domain.dto.UserWrapperResponse
import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.security.Authorize
import com.github.senocak.boilerplate.service.UserService
import com.github.senocak.boilerplate.util.AppConstants.ADMIN
import com.github.senocak.boilerplate.util.AppConstants.USER
import com.github.senocak.boilerplate.util.AppConstants.securitySchemeName
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import com.github.senocak.boilerplate.util.convertEntityToDto
import com.github.senocak.boilerplate.util.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Authorize(roles = [ADMIN, USER])
@RequestMapping(UserController.URL)
@Tag(name = "User", description = "User Controller")
class UserController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
): BaseController() {
    private val log: Logger by logger()

    @Throws(ServerException::class)
    @Operation(summary = "Get me", tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = UserWrapperResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @GetMapping(value = ["/me"])
    fun me(): UserWrapperResponse {
        val userResponse: UserResponse = userService.loggedInUser()!!.convertEntityToDto()
        return UserWrapperResponse(userResponse = userResponse, token = null)
    }

    @PatchMapping(value = ["/me"])
    @Operation(summary = "Update user by username", tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = HashMap::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun patchMe(
        @Parameter(description = "Request body to update", required = true) @Validated @RequestBody userDto: UpdateUserDto,
        resultOfValidation: BindingResult
    ): Map<String, String> {
        validate(resultOfValidation = resultOfValidation)
        val user: User? = userService.loggedInUser()
        val name: String? = userDto.name
        if (!name.isNullOrEmpty())
            user!!.name = name
        val password: String? = userDto.password
        val passwordConfirmation: String? = userDto.password_confirmation
        if (!password.isNullOrEmpty()) {
            if (passwordConfirmation.isNullOrEmpty()) {
                val message = "Password confirmation not provided"
                log.error(message)
                throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    variables = arrayOf(message), statusCode = HttpStatus.BAD_REQUEST)
            }
            if (passwordConfirmation != password) {
                val message = "Password and confirmation not matched"
                log.error(message)
                throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    variables = arrayOf(message), statusCode = HttpStatus.BAD_REQUEST)
            }
            user!!.password = passwordEncoder.encode(password)
        }
        userService.save(user = user!!)
        val response: MutableMap<String, String> = HashMap()
        response["message"] = "User updated."
        return response
    }

    companion object {
        const val URL = "/api/v1/user"
    }
}
