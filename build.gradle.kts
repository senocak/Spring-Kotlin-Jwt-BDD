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
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
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
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")

    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testcontainers:testcontainers:$testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainers")
    testImplementation("org.testcontainers:postgresql:$testcontainers")
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
}
