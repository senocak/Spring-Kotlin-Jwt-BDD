package com.github.senocak.boilerplate.controller

import com.github.senocak.boilerplate.domain.dto.ExceptionDto
import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.domain.dto.LoginRequest
import com.github.senocak.boilerplate.domain.dto.RegisterRequest
import com.github.senocak.boilerplate.domain.dto.RoleResponse
import com.github.senocak.boilerplate.domain.dto.UserResponse
import com.github.senocak.boilerplate.domain.dto.UserWrapperResponse
import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.security.JwtTokenProvider
import com.github.senocak.boilerplate.service.RoleService
import com.github.senocak.boilerplate.service.UserService
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import com.github.senocak.boilerplate.util.RoleName
import com.github.senocak.boilerplate.util.convertEntityToDto
import com.github.senocak.boilerplate.util.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = [AuthController.URL])
@Tag(name = "Authentication", description = "AUTH API")
class AuthController(
    private val userService: UserService,
    private val roleService: RoleService,
    private val tokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
): BaseController() {
    private val log: Logger by logger()

    @PostMapping(value = ["/login"])
    @Operation(summary = "Login Endpoint", tags = ["Authentication"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UserWrapperResponse::class))]),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    @Throws(exceptionClasses = [ServerException::class])
    fun login(
        @Parameter(description = "Request body to login", required = true) @Validated @RequestBody loginRequest: LoginRequest,
        resultOfValidation: BindingResult
    ): UserWrapperResponse {
        validate(resultOfValidation = resultOfValidation)
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )
        val user: User = userService.findByUsername(username = loginRequest.username!!)
        val login: UserResponse = user.convertEntityToDto()
        return generateUserWrapperResponse(userResponse = login)
    }

    @PostMapping(value = ["/register"])
    @Operation(summary = "Register Endpoint", tags = ["Authentication"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UserWrapperResponse::class))]),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    @Throws(exceptionClasses = [ServerException::class])
    fun register(
        @Parameter(description = "Request body to register", required = true) @Validated @RequestBody signUpRequest: RegisterRequest,
        resultOfValidation: BindingResult
    ): ResponseEntity<UserWrapperResponse> {
        validate(resultOfValidation = resultOfValidation)
        if (userService.existsByUsername(username = signUpRequest.username!!)) {
            log.error("Username:{} is already taken!", signUpRequest.username)
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                variables = arrayOf("Username is already taken!"), statusCode = HttpStatus.BAD_REQUEST)
        }
        if (userService.existsByEmail(email = signUpRequest.email!!)) {
            log.error("Email Address:{} is already taken!", signUpRequest.email)
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                variables = arrayOf("Email Address already in use!"), statusCode = HttpStatus.BAD_REQUEST)
        }
        val userRole: Role = roleService.findByName(RoleName.ROLE_USER)
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                variables = arrayOf("User Role is not found"), statusCode = HttpStatus.BAD_REQUEST)
                .also { log.error("User Role is not found") }
        val user = User(name = signUpRequest.name!!, username = signUpRequest.username!!, email = signUpRequest.email!!,
            password = passwordEncoder.encode(signUpRequest.password), roles = mutableListOf(userRole))
        val result: User = userService.save(user)
        log.info("User created. User: {}", result)
        val `object`: UserWrapperResponse? = try {
            login(LoginRequest(username = signUpRequest.username, password = signUpRequest.password), resultOfValidation)
        } catch (e: Exception) {
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                variables = arrayOf("Error occurred for generating jwt attempt", HttpStatus.INTERNAL_SERVER_ERROR.toString()),
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(`object`)
    }

    /**
     * Generate UserWrapperResponse with a given UserResponse
     * @param userResponse -- UserResponse that contains user data
     * @return UserWrapperResponse
     */
    private fun generateUserWrapperResponse(userResponse: UserResponse): UserWrapperResponse {
        val roles: List<String> = userResponse.roles.map { r: RoleResponse -> RoleName.fromString(r = r.name!!.name)!!.name }.toList()
        val jwtToken = tokenProvider.generateJwtToken(subject = userResponse.username, roles)
        val userWrapperResponse = UserWrapperResponse(userResponse = userResponse, token = jwtToken)
        log.info("UserWrapperResponse is generated. UserWrapperResponse: $userWrapperResponse")
        return userWrapperResponse
    }

    companion object {
        const val URL = "/api/v1/auth"
    }
}
