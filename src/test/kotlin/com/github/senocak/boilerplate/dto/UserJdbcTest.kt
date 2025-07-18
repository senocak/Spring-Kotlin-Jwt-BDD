package com.github.senocak.boilerplate.dto

import com.github.senocak.boilerplate.repository.UserJdbcService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals

@JdbcTest
@ActiveProfiles(value = ["jdbc-test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:tc:postgresql:15.3:///boilerplate?TC_INITSCRIPT",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.liquibase.enabled=false",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=validate"
    ]
)
@Sql(value = ["/migration/V1__init.sql", "/migration/V2__populate.sql"])
class UserJdbcTest {
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun whenInjectInMemoryDataSource_thenReturnCorrectEmployeeCount() {
        val userDao = UserJdbcService(jdbcTemplate = jdbcTemplate)
        assertEquals(expected = 2, actual = userDao.countOfUsers())
    }
}
