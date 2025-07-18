package com.github.senocak.boilerplate.config

import com.github.senocak.boilerplate.controller.AuthController
import com.github.senocak.boilerplate.controller.UserController
import com.github.senocak.boilerplate.security.JwtAuthenticationEntryPoint
import com.github.senocak.boilerplate.security.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val unauthorizedHandler: JwtAuthenticationEntryPoint,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    /**
     * Override this method to configure the HttpSecurity.
     * @param http -- It allows configuring web-based security for specific http requests
     * @throws Exception -- throws Exception
     */
    @Bean
    @Profile("!integration-test")
    fun securityFilterChainDSL(http: HttpSecurity, @Value("\${springdoc.api-docs.path}") path: String): SecurityFilterChain =
        http {
            csrf { disable() }
            exceptionHandling { this.authenticationEntryPoint = unauthorizedHandler }
            httpBasic { disable() }
            authorizeHttpRequests {
                authorize(pattern = "${AuthController.URL}/**", access = permitAll)
                authorize(pattern = "${UserController.URL}/**", access = permitAll)
                authorize(pattern = "/actuator/**", access = permitAll)
                authorize(pattern = "$path/**", access = permitAll)
                authorize(pattern = "/swagger**/**", access = permitAll)
                authorize(pattern = "/*.html", access = permitAll)
                authorize(matches = anyRequest, access = authenticated)
            }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            headers { frameOptions { disable() } }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(filter = jwtAuthenticationFilter)
        }
            .run { http.build() }
}
