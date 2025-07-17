package com.github.senocak.boilerplate.config.initializer

import com.github.senocak.boilerplate.TestConstants
import com.github.senocak.boilerplate.util.logger
import org.slf4j.Logger
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration
class PostgresqlInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val log: Logger by logger()

    private val postgresContainer: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:latest").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        withInitScript("migration/V1__init.sql")
        withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT)
    }

    init {
        postgresContainer.start()
    }

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        log.info("jdbc:postgresql://localhost:${postgresContainer.getMappedPort(1521)}/${postgresContainer.databaseName}?user=${postgresContainer.username}&password=${postgresContainer.password}&ssl=true")
        TestPropertyValues.of(
            "spring.datasource.url=" + postgresContainer.jdbcUrl,
            "spring.datasource.username=" + postgresContainer.username,
            "spring.datasource.password=" + postgresContainer.password
        ).applyTo(configurableApplicationContext.environment)
    }
}
