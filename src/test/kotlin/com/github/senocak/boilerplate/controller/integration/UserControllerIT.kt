package com.github.senocak.boilerplate.controller.integration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.boilerplate.TestConstants
import com.github.senocak.boilerplate.config.SpringBootTestConfig
import com.github.senocak.boilerplate.controller.UserController
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.domain.dto.UpdateUserDto
import com.github.senocak.boilerplate.exception.RestExceptionHandler
import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.service.UserService
import com.github.senocak.boilerplate.util.OmaErrorMessageType
import com.github.senocak.boilerplate.util.RoleName
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * This integration test class is written for
 * @see UserController
 * 5 tests
 */
@SpringBootTestConfig
@DisplayName(value = "Integration Tests for UserController")
class UserControllerIT {
    @Autowired private lateinit var userController: UserController
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var restExceptionHandler: RestExceptionHandler
    @MockitoSpyBean  private lateinit var userService: UserService

    private lateinit var mockMvc: MockMvc
    private lateinit var user: User

    @BeforeEach
    @Throws(exceptionClasses = [ServerException::class])
    fun beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(restExceptionHandler)
            .build()
        user = userService.findByUsername("asenocakUser")
        doReturn(value = user).`when`(userService).loggedInUser()
    }

    @Nested
    @Order(value = 1)
    @DisplayName(value = "Get me")
    internal inner class GetMeTest {
        @Test
        @DisplayName(value = "Happy Path")
        @Throws(exceptionClasses = [Exception::class])
        fun given_whenGetMe_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(UserController.URL + "/me")
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.name", IsEqual.equalTo(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", IsEqual.equalTo(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", IsEqual.equalTo(user.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles[0].name", IsEqual.equalTo(RoleName.ROLE_USER.role)))
        }
    }

    @Nested
    @Order(value = 2)
    @DisplayName(value = "Patch me")
    internal inner class PatchMeTest {
        private val updateUserDto: UpdateUserDto = UpdateUserDto()

        @Test
        @DisplayName(value = "ServerException is expected since schema is invalid")
        @Throws(exceptionClasses = [Exception::class])
        fun givenInvalidSchema_whenPatchMe_thenThrowServerException() {
            // Given
            updateUserDto.name = "as"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(UserController.URL + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(updateUserDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode", IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text", IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("name: size must be between 4 and 40")))
        }

        @Test
        @DisplayName(value = "ServerException is expected since password_confirmation is not valid")
        @Throws(exceptionClasses = [Exception::class])
        fun givenInvalidPassword_whenPatchMe_thenThrowServerException() {
            // Given
            updateUserDto.name = TestConstants.USER_NAME
            updateUserDto.password = TestConstants.USER_NAME
            updateUserDto.password_confirmation = "invalid"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(UserController.URL + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(updateUserDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(0)))
        }

        @Test
        @DisplayName(value = "Happy Path")
        @Throws(exceptionClasses = [Exception::class])
        fun given_whenPatchMe_thenReturn200() {
            // Given
            updateUserDto.name = TestConstants.USER_NAME
            updateUserDto.password = TestConstants.USER_PASSWORD
            updateUserDto.password_confirmation = TestConstants.USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch(UserController.URL + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(value = updateUserDto))
            // When
            val perform = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", IsEqual.equalTo("User updated.")))
        }
    }

    /**
     * @param value -- an object that want to be serialized
     * @return -- string
     * @throws JsonProcessingException -- throws JsonProcessingException
     */
    @Throws(JsonProcessingException::class)
    private fun writeValueAsString(value: Any): String =
        objectMapper.writeValueAsString(value)
}
