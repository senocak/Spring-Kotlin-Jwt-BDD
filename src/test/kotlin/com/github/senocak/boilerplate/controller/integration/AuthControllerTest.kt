package com.github.senocak.boilerplate.controller.integration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.boilerplate.TestConstants.USER_EMAIL
import com.github.senocak.boilerplate.TestConstants.USER_NAME
import com.github.senocak.boilerplate.TestConstants.USER_PASSWORD
import com.github.senocak.boilerplate.TestConstants.USER_USERNAME
import com.github.senocak.boilerplate.config.SpringBootTestConfig
import com.github.senocak.boilerplate.controller.AuthController
import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.dto.LoginRequest
import com.github.senocak.boilerplate.domain.dto.RegisterRequest
import com.github.senocak.boilerplate.exception.RestExceptionHandler
import com.github.senocak.boilerplate.repository.RoleRepository
import com.github.senocak.boilerplate.service.RoleService
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import com.github.senocak.boilerplate.util.RoleName
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * This integration test class is written for
 * @see AuthController
 * 8 tests
 */
@SpringBootTestConfig
@DisplayName(value = "Integration Tests for AuthController")
class AuthControllerTest {
    @Autowired private lateinit var authController: AuthController
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var restExceptionHandler: RestExceptionHandler
    @MockitoBean private lateinit var roleService: RoleService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(restExceptionHandler)
            .build()
    }

    @Nested
    @Order(value = 1)
    @DisplayName(value = "Test class for login scenarios")
    @TestMethodOrder(value = MethodOrderer.OrderAnnotation::class)
    internal inner class LoginTest {
        var loginRequest: LoginRequest = LoginRequest()

        @Test
        @Order(value = 1)
        @DisplayName(value = "ServerException is expected since request body is not valid")
        @Throws(exceptionClasses = [Exception::class])
        fun givenInvalidSchema_whenLogin_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = loginRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", containsInAnyOrder("username: must not be blank","password: must not be blank")))
        }

        @Test
        @Order(value = 2)
        @DisplayName(value = "ServerException is expected since credentials are not valid")
        @Throws(exceptionClasses = [Exception::class])
        fun givenInvalidCredentials_whenLogin_thenThrowServerException() {
            // Given
            loginRequest.username = "USERNAME"
            loginRequest.password = "PASSWORD"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = loginRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("User not found with email")))
        }

        @Test
        @Order(value = 3)
        @DisplayName(value = "Happy path")
        @Throws(exceptionClasses = [Exception::class])
        fun given_whenLogin_thenReturn200() {
            // Given
            loginRequest.username = "asenocakUser"
            loginRequest.password = "asenocak"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = loginRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", IsEqual.equalTo(loginRequest.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles[0].name", IsEqual.equalTo(RoleName.ROLE_USER.role)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token", IsNull.notNullValue()))
        }
    }

    @Nested
    @Order(value = 2)
    @DisplayName(value = "Test class for register scenarios")
    @TestMethodOrder(value = MethodOrderer.OrderAnnotation::class)
    internal inner class RegisterTest {
        private var registerRequest: RegisterRequest = RegisterRequest()

        @Test
        @Order(value = 1)
        @DisplayName(value = "ServerException is expected since request body is not valid")
        @Throws(exceptionClasses = [Exception::class])
        fun givenInvalidSchema_whenRegister_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = registerRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", containsInAnyOrder("password: must not be blank","username: must not be blank",
                        "name: must not be blank","email: Invalid email")))
        }

        @Test
        @Order(value = 2)
        @DisplayName(value = "ServerException is expected since there is already user with username")
        @Throws(exceptionClasses = [Exception::class])
        fun givenUserNameExist_whenRegister_thenThrowServerException() {
            // Given
            registerRequest.name = USER_NAME
            registerRequest.username = USER_USERNAME
            registerRequest.email = USER_EMAIL
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = registerRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("Username is already taken!")))
        }

        @Test
        @Order(value = 3)
        @DisplayName(value = "ServerException is expected since there is already user with email")
        @Throws(exceptionClasses = [Exception::class])
        fun givenEmailExist_whenRegister_thenThrowServerException() {
            // Given
            registerRequest.name = USER_NAME
            registerRequest.username = "USER_USERNAME"
            registerRequest.email = USER_EMAIL
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = registerRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("Email Address already in use!")))
        }

        @Test
        @Order(value = 4)
        @DisplayName(value = "ServerException is expected since invalid role")
        @Throws(exceptionClasses = [Exception::class])
        fun givenNullRole_whenRegister_thenThrowServerException() {
            // Given
            doReturn(value = null).`when`(roleService).findByName(roleName = RoleName.ROLE_USER)
            registerRequest.name = USER_NAME
            registerRequest.username = "USER_USERNAME"
            registerRequest.email = "userNew@email.com"
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = registerRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.MANDATORY_INPUT_MISSING.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.MANDATORY_INPUT_MISSING.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("User Role is not found")))
        }

        @Test
        @Order(value = 5)
        @DisplayName(value = "Happy path")
        @Throws(exceptionClasses = [Exception::class])
        fun given_whenRegister_thenReturn201() {
            // Given
            val role: Role? = roleRepository.findByName(roleName = RoleName.ROLE_USER)
            doReturn(value = role).`when`(roleService).findByName(roleName = RoleName.ROLE_USER)
            registerRequest.name = USER_NAME
            registerRequest.username = "USER_USERNAME"
            registerRequest.email = "userNew@email.com"
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post(AuthController.URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = registerRequest))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", IsEqual.equalTo("USER_USERNAME")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles[0].name", IsEqual.equalTo(RoleName.ROLE_USER.role)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token", IsNull.notNullValue()))
        }
    }

    /**
     * @param value -- an object that want to be serialized
     * @return -- string
     * @throws JsonProcessingException -- throws JsonProcessingException
     */
    @Throws(exceptionClasses = [JsonProcessingException::class])
    private fun writeValueAsString(value: Any): String =
        objectMapper.writeValueAsString(value)
}
