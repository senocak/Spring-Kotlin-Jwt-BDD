package com.github.senocak.boilerplate.config

import com.github.senocak.boilerplate.config.initializer.PostgresqlInitializer
import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME
import io.cucumber.spring.CucumberContextConfiguration
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestClassOrder
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Tag(value = "cucumber")
@Target(allowedTargets = [AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS])
@Retention(value = AnnotationRetention.RUNTIME)
@ActiveProfiles(value = ["cucumber-test"])
@TestClassOrder(value = ClassOrderer.OrderAnnotation::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(initializers = [
    PostgresqlInitializer::class,
])
@TestPropertySource(value = ["/application-cucumber-test.yml"])
@Suite
@CucumberContextConfiguration
@IncludeEngines(value = ["cucumber"])
@SelectClasspathResource(value = "features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.github.senocak.boilerplate.stepdefs")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
annotation class CucumberTestConfig
