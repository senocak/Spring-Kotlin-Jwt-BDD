package com.github.senocak.boilerplate.service

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.repository.RoleRepository
import com.github.senocak.boilerplate.util.RoleName
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag(value = "unit")
@ExtendWith(value = [MockitoExtension::class])
@DisplayName(value = "Unit Tests for RoleService")
class RoleServiceTest {
    private val roleRepository: RoleRepository = mock()
    private var roleService = RoleService(roleRepository = roleRepository)

    @Test
    fun givenRoleName_whenFindByName_thenAssertResult() {
        // Given
        val role = Role()
        val roleName: RoleName = RoleName.ROLE_USER
        doReturn(value = role).`when`(roleRepository).findByName(roleName = roleName)
        // When
        val findByName: Role? = roleService.findByName(roleName = roleName)
        // Then
        assertEquals(expected = role, actual = findByName)
    }

    @Test
    fun givenNullRoleName_whenFindByName_thenAssertResult() {
        // When
        val findByName: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        // Then
        assertNull(actual = findByName)
    }
}
