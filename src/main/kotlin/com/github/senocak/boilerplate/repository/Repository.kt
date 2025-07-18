package com.github.senocak.boilerplate.repository

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.util.RoleName
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface RoleRepository: PagingAndSortingRepository<Role, Long> {
    fun findByName(roleName: RoleName): Role?
}

interface UserRepository: CrudRepository<User, String>, PagingAndSortingRepository<User, String> {
    fun findByEmail(email: String?): User?

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    fun findByUsername(@org.springframework.data.repository.query.Param("username") username: String?): User?

    fun existsByUsername(username: String?): Boolean

    fun existsByEmail(email: String?): Boolean
}
