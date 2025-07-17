package com.github.senocak.boilerplate.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    var jwtSecret: String,
    var jwtExpirationInMs: String
)
