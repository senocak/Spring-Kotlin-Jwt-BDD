package com.github.senocak.boilerplate.util

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.domain.dto.RoleResponse
import com.github.senocak.boilerplate.domain.dto.UserResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @return -- UserResponse object
 */
fun User.convertEntityToDto(): UserResponse =
    UserResponse(name = this.name, email = this.email, username = this.username,
            roles = this.roles.stream().map { it: Role -> it.convertEntityToDto() }.toList())

/**
 * @return -- RoleResponse object
 */
fun Role.convertEntityToDto(): RoleResponse =
    RoleResponse().also { it: RoleResponse ->
        it.name = this.name
    }

fun <R : Any> R.logger(): Lazy<Logger> = lazy {
    LoggerFactory.getLogger((if (javaClass.kotlin.isCompanion) javaClass.enclosingClass else javaClass).name)
}
