package com.github.senocak.boilerplate.repository

import org.springframework.jdbc.core.JdbcTemplate

class UserJdbcService(private val jdbcTemplate: JdbcTemplate) {

    fun countOfUsers(): Int =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Int::class.java)!!
}
