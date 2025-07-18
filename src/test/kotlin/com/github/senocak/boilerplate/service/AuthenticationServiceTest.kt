package com.github.senocak.boilerplate.service

import com.github.senocak.boilerplate.TestConstants
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.nio.file.AccessDeniedException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag(value = "unit")
@ExtendWith(value = [MockitoExtension::class])
@DisplayName(value = "Unit Tests for AuthenticationService")
class AuthenticationServiceTest {
    private val authenticationService = AuthenticationService()
    var auth: Authentication = mock<Authentication>()
    //val roleAdmin = SimpleGrantedAuthority("ROLE_ADMIN")
    val roleAdmin: SimpleGrantedAuthority = mock()
    val authorities: MutableList<GrantedAuthority> = arrayListOf(roleAdmin)
    var user: User = User(TestConstants.USER_USERNAME, TestConstants.USER_PASSWORD, authorities)

    @BeforeEach
    fun initSecurityContext() {
        SecurityContextHolder.getContext().authentication = auth
    }

    @Test
    fun givenNullAuthenticationWhenIsAuthorizedThenThrowAccessDeniedException() {
        // When
        val closureToTest = Executable { authenticationService.isAuthorized(aInRoles = arrayOf()) }
        // Then
        assertThrows(AccessDeniedException::class.java, closureToTest)
    }

    @Test
    @Throws(exceptionClasses = [AccessDeniedException::class])
    fun givenWhenIsAuthorizedThenAssertResult() {
        // Given
        doReturn(value = user).`when`(auth).principal
        whenever(methodCall = roleAdmin.authority).thenReturn("ROLE_ADMIN")
        // When
        val preHandle: Boolean = authenticationService.isAuthorized(aInRoles = arrayOf("ADMIN"))
        // Then
        assertTrue(actual = preHandle)
    }

    @Test
    @Throws(exceptionClasses = [AccessDeniedException::class])
    fun givenNotValidRoleWhenIsAuthorizedThenAssertResult() {
        // Given
        doReturn(value = user).`when`(auth).principal
        // When
        val preHandle: Boolean = authenticationService.isAuthorized(aInRoles = arrayOf("USER"))
        // Then
        assertFalse(actual = preHandle)
    }

    @Test
    fun givenNullAuthenticationInRolesWhenIsAuthorizedThenThrowAccessDeniedException() {
        // Given
        doReturn(value = user).`when`(auth).principal
        doThrow(toBeThrown = RuntimeException::class).`when`(roleAdmin).authority
        // When
        val closureToTest = Executable { authenticationService.isAuthorized(aInRoles = arrayOf("roleAdmin")) }
        // Then
        assertThrows(AccessDeniedException::class.java, closureToTest)
    }
}
