package com.github.senocak.boilerplate.factory

import com.github.senocak.boilerplate.TestConstants.USER_EMAIL
import com.github.senocak.boilerplate.TestConstants.USER_NAME
import com.github.senocak.boilerplate.TestConstants.USER_PASSWORD
import com.github.senocak.boilerplate.TestConstants.USER_USERNAME
import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.util.RoleName

/**
 * Creates a new user with the given name, username, email, password and roles.
 * @return the new user
 */
fun createUser(): User =
    User(name = USER_NAME, username = USER_USERNAME, email = USER_EMAIL, password = USER_PASSWORD,
        roles = mutableListOf(
        createRole(roleName = RoleName.ROLE_USER),
        createRole(roleName = RoleName.ROLE_ADMIN)))

/**
 * Creates a new role with the given name.
 * @param roleName the name of the role
 * @return the new role
 */
fun createRole(roleName: RoleName?): Role =
    Role().also { it: Role ->
        it.name = roleName
    }
