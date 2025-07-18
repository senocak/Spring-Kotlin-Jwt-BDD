package com.github.senocak.boilerplate.service

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.domain.dto.RoleResponse
import com.github.senocak.boilerplate.domain.dto.UserResponse
import com.github.senocak.boilerplate.factory.createUser
import com.github.senocak.boilerplate.util.RoleName
import com.github.senocak.boilerplate.util.convertEntityToDto
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@Tag(value = "unit")
@ExtendWith(value = [MockitoExtension::class])
@DisplayName(value = "Unit Tests for Extension Functions")
class DtoConverterTest {

    @Test
    fun givenUser_whenConvertEntityToDto_thenAssertResult() {
        // Given
        val user: User = createUser()
        // When
        val convertEntityToDto: UserResponse = user.convertEntityToDto()
        // Then
        assertEquals(expected = user.name, actual = convertEntityToDto.name)
        assertEquals(expected = user.email, actual = convertEntityToDto.email)
        assertEquals(expected = user.username, actual = convertEntityToDto.username)
    }

    @Test
    fun givenRole_whenConvertEntityToDto_thenAssertResult() {
        // Given
        val role = Role()
        role.name = RoleName.ROLE_USER
        // When
        val convertEntityToDto: RoleResponse = role.convertEntityToDto()
        // Then
        assertEquals(expected = role.name, actual = convertEntityToDto.name)
    }
}
