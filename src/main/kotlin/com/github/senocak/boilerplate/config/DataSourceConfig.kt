package com.github.senocak.boilerplate.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
class DataSourceConfig: DataSourceProperties() {
    lateinit var hikari: HikariConfig

    @Bean
    @Primary
    fun dataSource(): DataSource =
        when {
            determineDriverClassName() == "org.postgresql.Driver" -> DriverManagerDataSource()
                .also { db: DriverManagerDataSource ->
                    db.url = url
                    db.username = username
                    db.password = password
                }
            else -> throw RuntimeException("Not configured")
        }

    @Bean
    fun hikariDataSource(dataSource: DataSource): HikariDataSource =
        HikariDataSource(HikariConfig()
            .also { hds: HikariConfig ->
                hds.dataSource = dataSource
                hds.poolName = hikari.poolName ?: "SpringKotlinJPAHikariCP"
                hds.minimumIdle = hikari.minimumIdle
                hds.maximumPoolSize = hikari.maximumPoolSize
                hds.maxLifetime = hikari.maxLifetime
                hds.idleTimeout = hikari.idleTimeout
                hds.connectionTimeout = hikari.connectionTimeout
                hds.transactionIsolation = hikari.transactionIsolation ?: "TRANSACTION_READ_COMMITTED"
            }
        )
}
