package com.github.senocak.boilerplate.service

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.domain.User
import com.github.senocak.boilerplate.exception.ServerException
import com.github.senocak.boilerplate.repository.UserRepository
import com.github.senocak.boilerplate.util.RoleName
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository): UserDetailsService {

    /**
     * @param username -- string username to find in db
     * @return -- Optional User object
     */
    fun findByUsername(username: String): User =
        userRepository.findByUsername(username = username) ?: throw UsernameNotFoundException("User not found with email")

    /**
     * @param username -- string username to find in db
     * @return -- Optional User object
     */
    fun existsByUsername(username: String): Boolean =
        userRepository.existsByUsername(username = username)

    /**
     * @param email -- string email to find in db
     * @return -- true or false
     */
    fun existsByEmail(email: String): Boolean =
        userRepository.existsByEmail(email = email)

    /**
     * @param email -- string email to find in db
     * @return -- User object
     * @throws UsernameNotFoundException -- throws UsernameNotFoundException
     */
    @Throws(exceptionClasses = [UsernameNotFoundException::class])
    fun findByEmail(email: String): User =
        userRepository.findByEmail(email = email) ?: throw UsernameNotFoundException("User not found with email")

    /**
     * @param user -- User object to persist to db
     * @return -- User object that is persisted to db
     */
    fun save(user: User): User =
        userRepository.save(user)

    /**
     * @param username -- username
     * @return -- Spring User object
     */
    @Transactional
    @Throws(exceptionClasses = [UsernameNotFoundException::class])
    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.User {
        val user: User = findByUsername(username = username)
        val authorities: List<GrantedAuthority> = user.roles
            .map { r: Role -> SimpleGrantedAuthority(RoleName.fromString(r = r.name.toString())!!.name) }
            .toList()
        return org.springframework.security.core.userdetails.User(user.username, user.password, authorities)
    }

    /**
     * @return -- User entity that is retrieved from db
     * @throws ServerException -- throws ServerException
     */
    @Throws(exceptionClasses = [ServerException::class])
    fun loggedInUser(): User? {
        val username = (SecurityContextHolder.getContext().authentication.principal as org.springframework.security.core.userdetails.User).username
        return findByUsername(username = username)
    }
}
