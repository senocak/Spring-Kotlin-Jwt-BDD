package com.github.senocak.boilerplate.service

import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.factory.createUser
import com.github.senocak.boilerplate.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Tag(value = "unit")
@ExtendWith(value = [MockitoExtension::class])
@DisplayName(value = "Unit Tests for UserService")
class UserServiceTest {
    private val userRepository: UserRepository = mock()
    private var userService = UserService(userRepository = userRepository)
    private var auth: Authentication = mock()
    private var user: User = mock()

    @Test
    fun givenUsername_whenFindByUsername_thenAssertResult() {
        // Given
        val user: User = createUser()
        doReturn(value = user).`when`(userRepository).findByUsername(username = "username")
        // When
        val findByUsername: User = userService.findByUsername(username = "username")
        // Then
        assertEquals(expected = user, actual = findByUsername)
    }

    @Test
    fun givenNullUsername_whenFindByUsername_thenAssertResult() {
        // When
        val closureToTest = Executable { userService.findByUsername(username = "username") }
        // Then
        assertThrows(UsernameNotFoundException::class.java, closureToTest)
    }

    @Test
    fun givenUsername_whenExistsByUsername_thenAssertResult() {
        // When
        val existsByUsername = userService.existsByUsername(username = "username")
        // Then
        assertFalse(actual = existsByUsername)
    }

    @Test
    fun givenUsername_whenExistsByEmail_thenAssertResult() {
        // When
        val existsByEmail = userService.existsByEmail(email = "username")
        // Then
        assertFalse(actual = existsByEmail)
    }

    @Test
    fun givenEmail_whenFindByUsername_thenAssertResult() {
        // Given
        val user: User = createUser()
        doReturn(value = user).`when`(userRepository).findByEmail(email = "Email")
        // When
        val findByEmail: User = userService.findByEmail(email = "Email")
        // Then
        assertEquals(expected = user, actual = findByEmail)
    }

    @Test
    fun givenNullEmail_whenFindByEmail_thenAssertResult() {
        // When
        val closureToTest = Executable { userService.findByEmail(email = "Email") }
        // Then
        assertThrows(UsernameNotFoundException::class.java, closureToTest)
    }

    @Test
    fun givenUser_whenSave_thenAssertResult() {
        // Given
        val user: User = createUser()
        doReturn(value = user).`when`(userRepository).save<User>(user)
        // When
        val save: User = userService.save(user)
        // Then
        assertEquals(expected = user, actual = save)
    }

    @Test
    fun givenUser_whenCreate_thenAssertResult() {
        // Given
        val user: User = createUser()
        whenever(methodCall = userRepository.save(user)).thenReturn(user)
        // When
        val create: User = userService.save(user = user)
        // Then
        assertEquals(expected = user, actual = create)
    }

    @Test
    fun givenNullUsername_whenLoadUserByUsername_thenAssertResult() {
        // When
        val closureToTest = Executable { userService.loadUserByUsername(username = "username") }
        // Then
        assertThrows(UsernameNotFoundException::class.java, closureToTest)
    }

    @Test
    fun givenUsername_whenLoadUserByUsername_thenAssertResult() {
        // Given
        val user: User = createUser()
        doReturn(value = user).`when`(userRepository).findByUsername(username = "username")
        // When
        val loadUserByUsername = userService.loadUserByUsername(username = "username")
        // Then
        assertEquals(expected = user.username, actual = loadUserByUsername.username)
    }

    @Test
    fun givenNotLoggedIn_whenLoadUserByUsername_thenAssertResult() {
        // Given
        SecurityContextHolder.getContext().authentication = auth
        doReturn(value = user).`when`(auth).principal
        doReturn(value = "user").`when`(user).username
        // When
        val closureToTest = Executable { userService.loggedInUser() }
        // Then
        assertThrows(UsernameNotFoundException::class.java, closureToTest)
    }

    @Test
    @Throws(exceptionClasses = [UsernameNotFoundException::class])
    fun givenLoggedIn_whenLoadUserByUsername_thenAssertResult() {
        // Given
        SecurityContextHolder.getContext().authentication = auth
        doReturn(user).`when`(auth).principal
        doReturn(value = "username").`when`(user).username
        val user: User = createUser()
        doReturn(value = user).`when`(userRepository).findByUsername(username = "username")
        // When
        val loggedInUser: User? = userService.loggedInUser()
        // Then
        assertEquals(expected = user.username, actual = loggedInUser!!.username)
    }
}
