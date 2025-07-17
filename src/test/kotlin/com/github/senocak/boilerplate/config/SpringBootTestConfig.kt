package com.github.senocak.boilerplate.config

import com.github.senocak.boilerplate.config.initializer.PostgresqlInitializer
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Tag(value = "integration")
@Target(allowedTargets = [AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS])
@ExtendWith(value = [SpringExtension::class])
@Retention(value = AnnotationRetention.RUNTIME)
@ActiveProfiles(value = ["integration-test"])
@TestClassOrder(value = ClassOrderer.OrderAnnotation::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@Import(TestConfig::class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(initializers = [
    PostgresqlInitializer::class,
])
//@TestPropertySource({"/application-integration-test.yml" })
annotation class SpringBootTestConfig 
