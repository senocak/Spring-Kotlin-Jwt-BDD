package com.github.senocak.boilerplate.service

import com.github.senocak.boilerplate.util.logger
import org.slf4j.Logger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import java.nio.file.AccessDeniedException

@Service
class AuthenticationService {
    private val log: Logger by logger()
    var authorizationFailed = "Authentication error"

    /**
     * Getting username from the security context
     * @param aInRoles -- roles that a user must have
     * @return  -- username or null
     * @throws AccessDeniedException -- if a user does not have required roles
     */
    @Throws(exceptionClasses = [AccessDeniedException::class])
    fun isAuthorized(aInRoles: Array<String>): Boolean {
        val getPrinciple = getPrinciple() ?: throw AccessDeniedException(authorizationFailed)
            .also { log.warn("AccessDeniedException occurred") }
        try {
            for (role in aInRoles) {
                for (authority in getPrinciple.authorities) {
                    if (authority.authority == "ROLE_$role") {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Exception occurred while checking roles. ${e.localizedMessage}")
            throw AccessDeniedException(authorizationFailed)
        }
        return false
    }

    /**
     * Getting a user object in the security context
     * @return -- user object or null
     */
    fun getPrinciple(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
        return try {
            if (authentication.principal is User) {
                authentication.principal as User
            } else null
        } catch (e: Exception) {
            log.warn("Exception occurred, returning null. ${e.localizedMessage}")
            null
        }
    }
}
