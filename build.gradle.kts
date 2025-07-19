plugins {
    kotlin(module = "jvm") version "1.9.25"
    kotlin(module = "plugin.spring") version "1.9.25"
    id(id = "org.springframework.boot") version "3.5.3"
    id(id = "io.spring.dependency-management") version "1.1.7"
    kotlin(module = "plugin.jpa") version "1.9.25"
}

group = "com.github.senocak.boilerplate"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

springBoot {
    buildInfo {
        properties {
            this.name = "Spring Kotlin Boilerplate Application"
        }
    }
}

val jjwt = "0.12.6"
val testcontainers = "1.21.3"
val swagger = "2.8.9"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    implementation("com.github.gavlyukovskiy:datasource-proxy-spring-boot-starter:1.10.0")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:$swagger")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$swagger")
    implementation("io.jsonwebtoken:jjwt-api:$jjwt")
    implementation("io.jsonwebtoken:jjwt-impl:$jjwt")
    implementation("io.jsonwebtoken:jjwt-jackson:$jjwt")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testcontainers:testcontainers:$testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainers")
    testImplementation("org.testcontainers:postgresql:$testcontainers")

    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("io.cucumber:cucumber-java:7.23.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.23.0")
    testImplementation("io.cucumber:cucumber-spring:7.23.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation(fqName = "jakarta.persistence.Entity")
    annotation(fqName = "jakarta.persistence.MappedSuperclass")
    annotation(fqName = "jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    maxHeapSize = "1G"
    if (project.hasProperty("skipTests")) {
        val skipTestsValue: List<String> = project.property("skipTests")
            .toString()
            .split(",")
            .map { it: String -> it.trim() }
        when {
            skipTestsValue.contains(element = "all") -> {
                println(message = "Skipping all tests")
                enabled = false
            }
            skipTestsValue.contains(element = "unit") && skipTestsValue.contains(element = "integration") -> {
                println(message = "Skipping both unit and integration tests")
                enabled = false
            }
            skipTestsValue.contains(element = "unit") -> {
                println(message = "Skipping unit tests")
                exclude("**/*Test.*")
            }
            skipTestsValue.contains(element = "integration") -> {
                println(message = "Skipping integration tests")
                exclude("**/*IT.*")
            }
            else -> println(message = "No valid skipTests value provided; running all tests")
        }
    }
}

tasks.register<Test>(name = "integrationTest") {
    description = "Runs the integration tests"
    group = "Verification"
    include("**/*IT.*")
    useJUnitPlatform()
    maxHeapSize = "1G"
}
