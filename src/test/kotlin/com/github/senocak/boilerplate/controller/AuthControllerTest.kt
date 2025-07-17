package com.github.senocak.boilerplate.controller

import com.github.senocak.boilerplate.TestConstants.USER_EMAIL
import com.github.senocak.boilerplate.TestConstants.USER_NAME
import com.github.senocak.boilerplate.TestConstants.USER_PASSWORD
import com.github.senocak.boilerplate.TestConstants.USER_USERNAME
import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.domain.dto.LoginRequest
import com.github.senocak.boilerplate.domain.dto.RegisterRequest
import com.github.senocak.boilerplate.domain.dto.UserWrapperResponse
import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.security.JwtTokenProvider
import com.github.senocak.boilerplate.service.RoleService
import com.github.senocak.boilerplate.service.UserService
import com.github.senocak.boilerplate.util.RoleName
import com.github.senocak.boilerplate.factory.UserFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers.anyList
import org.mockito.InjectMocks
import org.mockito.Mockito.mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Tag(value = "unit")
@ExtendWith(value = [MockitoExtension::class])
@DisplayName(value = "Unit Tests for AuthController")
class AuthControllerTest {
    @InjectMocks var authController: AuthController? = null

    private val userService: UserService = mock(UserService::class.java)
    private val roleService: RoleService = mock(RoleService::class.java)
    private val tokenProvider: JwtTokenProvider = mock(JwtTokenProvider::class.java)
    private val authenticationManager: AuthenticationManager = mock(AuthenticationManager::class.java)
    private val authentication: Authentication = mock(Authentication::class.java)
    private val passwordEncoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val bindingResult: BindingResult = mock(BindingResult::class.java)

    var user: User = UserFactory.createUser()

    @Nested
    internal inner class LoginTest {
        private val loginRequest: LoginRequest = LoginRequest()
        @BeforeEach
        fun setup() {
            loginRequest.username = USER_NAME
            loginRequest.password = USER_PASSWORD
        }

        @Test
        @Throws(ServerException::class)
        fun givenSuccessfulPath_whenLogin_thenReturn200() {
            // Given
            whenever(methodCall = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(
                loginRequest.username, loginRequest.password))).thenReturn(authentication)
            whenever(methodCall = userService.findByUsername(username = loginRequest.username!!)).thenReturn(user)
            val generatedToken = "generatedToken"
            whenever(methodCall = tokenProvider.generateJwtToken(subject = eq(value = user.username), roles = anyList())).thenReturn(generatedToken)
            // When
            val response: UserWrapperResponse = authController!!.login(loginRequest = loginRequest, resultOfValidation = bindingResult)
            // Then
            assertNotNull(actual = response)
            assertNotNull(actual = response.userResponse)
            assertEquals(expected = generatedToken, actual = response.token)
            assertEquals(expected = user.name, actual = response.userResponse.name)
            assertEquals(expected = user.username, actual = response.userResponse.username)
            assertEquals(expected = user.email, actual = response.userResponse.email)
            assertEquals(expected = user.roles.size, actual = response.userResponse.roles.size)
        }
    }

    @Nested
    internal inner class RegisterTest {
        private val registerRequest: RegisterRequest = RegisterRequest()

        @BeforeEach
        fun setup() {
            registerRequest.name = USER_NAME
            registerRequest.username = USER_USERNAME
            registerRequest.email = USER_EMAIL
            registerRequest.password = USER_PASSWORD
        }

        @Test
        fun givenExistUserName_whenRegister_thenThrowServerException() {
            // Given
            whenever(methodCall = userService.existsByUsername(username = registerRequest.username!!)).thenReturn(true)
            // When
            val closureToTest = Executable { authController!!.register(signUpRequest = registerRequest, resultOfValidation = bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        fun givenExistMail_whenRegister_thenThrowServerException() {
            // Given
            whenever(methodCall = userService.existsByEmail(email = registerRequest.email!!)).thenReturn(true)
            // When
            val closureToTest = Executable { authController!!.register(signUpRequest = registerRequest, resultOfValidation = bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        fun givenNotValidRole_whenRegister_thenThrowServerException() {
            // Given
            whenever(methodCall = roleService.findByName(roleName = RoleName.ROLE_USER)).thenReturn(null)
            // When
            val closureToTest = Executable { authController!!.register(signUpRequest = registerRequest, resultOfValidation = bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        fun givenNotLogin_whenRegister_thenThrowServerException() {
            // Given
            doReturn(Role()).`when`(roleService).findByName(RoleName.ROLE_USER)
            // When
            val closureToTest = Executable { authController!!.register(registerRequest, bindingResult!!) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenRegister_thenReturn201() {
            // Given
            whenever(methodCall = roleService.findByName(roleName = RoleName.ROLE_USER)).thenReturn(Role())
            whenever(methodCall = userService.save(user = user)).thenReturn(user)
            whenever(methodCall = userService.findByUsername(username = registerRequest.username!!)).thenReturn(user)
            val generatedToken = "generatedToken"
            whenever(methodCall = tokenProvider.generateJwtToken(subject = eq(value = user.username), roles = anyList())).thenReturn(generatedToken)
            // When
            val response: ResponseEntity<UserWrapperResponse> = authController!!.register(signUpRequest = registerRequest, resultOfValidation = bindingResult)
            // Then
            assertNotNull(actual = response)
            assertNotNull(actual = response.body)
            assertEquals(expected = HttpStatus.CREATED, actual = response.statusCode)
            assertNotNull(actual = response.body!!.userResponse)
            assertNotNull(actual = response.body!!.token)
            assertEquals(expected = user.name, actual = response.body!!.userResponse.name)
            assertEquals(expected = user.username, actual = response.body!!.userResponse.username)
            assertEquals(expected = user.email, actual = response.body!!.userResponse.email)
            assertEquals(expected = user.roles.size, actual = response.body!!.userResponse.roles.size)
        }
    }
}
