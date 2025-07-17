package com.github.senocak.boilerplate.controller

import com.github.senocak.boilerplate.TestConstants
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.domain.dto.UpdateUserDto
import com.github.senocak.boilerplate.domain.dto.UserWrapperResponse
import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.service.UserService
import com.github.senocak.boilerplate.factory.UserFactory.createUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Tag(value = "unit")
@ExtendWith(value = [MockitoExtension::class])
@DisplayName(value = "Unit Tests for UserController")
class UserControllerTest {
    private val userService: UserService = Mockito.mock(UserService::class.java)
    private val passwordEncoder: PasswordEncoder = Mockito.mock(PasswordEncoder::class.java)
    private var userController: UserController = UserController(userService, passwordEncoder)
    private val bindingResult: BindingResult = Mockito.mock(BindingResult::class.java)

    @Nested
    internal inner class GetMeTest {
        
        @Test
        @Throws(ServerException::class)
        fun givenServerException_whenGetMe_thenThrowServerException() {
            // Given
            doThrow(toBeThrown = ServerException::class).`when`(userService).loggedInUser()
            // When
            val closureToTest = Executable { userController.me() }
            // Then
            assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenGetMe_thenReturn200() {
            // Given
            val user: User = createUser()
            doReturn(value = user).`when`(userService).loggedInUser()
            // When
            val getMe: UserWrapperResponse = userController.me()
            // Then
            assertNotNull(actual = getMe)
            assertNotNull(actual = getMe.userResponse)
            assertEquals(expected = user.username, actual = getMe.userResponse.username)
            assertEquals(expected = user.email, actual = getMe.userResponse.email)
            assertEquals(expected = user.name, actual = getMe.userResponse.name)
            assertNull(actual = getMe.token)
        }
    }

    @Nested
    internal inner class PatchMeTest {
        private val updateUserDto: UpdateUserDto = UpdateUserDto()

        @Test
        @Throws(ServerException::class)
        fun givenNullPasswordConf_whenPatchMe_thenThrowServerException() {
            // Given
            val user: User = createUser()
            doReturn(value = user).`when`(userService).loggedInUser()
            updateUserDto.password = "pass1"
            // When
            val closureToTest = Executable { userController.patchMe(userDto = updateUserDto, resultOfValidation = bindingResult) }
            // Then
            assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun givenInvalidPassword_whenPatchMe_thenThrowServerException() {
            // Given
            val user: User = createUser()
            doReturn(value = user).`when`(userService).loggedInUser()
            updateUserDto.password = "pass1"
            updateUserDto.password_confirmation = "pass2"
            // When
            val closureToTest = Executable { userController.patchMe(userDto = updateUserDto, resultOfValidation = bindingResult) }
            // Then
            assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenPatchMe_thenThrowServerException() {
            // Given
            val user: User = createUser()
            doReturn(value = user).`when`(userService).loggedInUser()
            updateUserDto.name = TestConstants.USER_NAME
            updateUserDto.password = "pass1"
            updateUserDto.password_confirmation = "pass1"
            // When
            val patchMe: Map<String, String> = userController.patchMe(userDto = updateUserDto, resultOfValidation = bindingResult)
            // Then
            assertNotNull(actual = patchMe)
            assertEquals(expected = 1, actual = patchMe.size)
            assertNotNull(actual = patchMe["message"])
            assertEquals(expected = "User updated.", actual = patchMe["message"])
        }
    }
}
