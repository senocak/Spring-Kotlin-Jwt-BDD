package com.github.senocak.boilerplate.security

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.service.UserService
import com.github.senocak.boilerplate.util.RoleName
import com.github.senocak.boilerplate.util.logger
import org.slf4j.Logger
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
): AuthenticationManager {
    private val log: Logger by logger()

    override fun authenticate(authentication: Authentication): Authentication {
        val user: User? = userService.findByUsername(authentication.name)
        if (authentication.credentials != null){
            val matches = passwordEncoder.matches(authentication.credentials.toString(), user!!.password)
            if (!matches) {
                log.error("AuthenticationCredentialsNotFoundException occurred for ${user.name}")
                throw AuthenticationCredentialsNotFoundException("Username or password invalid")
            }
        }
        val authorities: MutableCollection<SimpleGrantedAuthority> = ArrayList()
        authorities.add(element = SimpleGrantedAuthority(RoleName.ROLE_USER.role))
        if (user!!.roles.any { r: Role -> r.name!! == RoleName.ROLE_ADMIN })
            authorities.add(element = SimpleGrantedAuthority(RoleName.ROLE_ADMIN.role))

        val loadUserByUsername = userService.loadUserByUsername(username = authentication.name)
        val auth: Authentication = UsernamePasswordAuthenticationToken(loadUserByUsername, user.password, authorities)
        SecurityContextHolder.getContext().authentication = auth
        log.debug("Authentication is set to SecurityContext for ${user.name}")
        return auth
    }
}
