package com.github.senocak.boilerplate.repository

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.util.RoleName
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.Optional

interface RoleRepository: PagingAndSortingRepository<Role, Long> {
    fun findByName(roleName: RoleName): Role?
}

interface UserRepository: CrudRepository<User, String>, PagingAndSortingRepository<User, String> {
    fun findByUsernameOrEmail(username: String?, email: String?): User?
    fun findByIdIn(userIds: List<String?>?): List<User?>?
    fun findByEmail(email: String?): User?
    fun findByUsername(username: String?): User?
    fun existsByUsername(username: String?): Boolean
    fun existsByEmail(email: String?): Boolean
}
