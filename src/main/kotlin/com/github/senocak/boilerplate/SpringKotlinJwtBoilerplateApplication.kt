package com.github.senocak.boilerplate

import com.github.senocak.boilerplate.util.AppConstants.getLogger
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringKotlinJwtBoilerplateApplication {
    @EventListener(value = [ApplicationReadyEvent::class])
    fun init() {
        getLogger().debug("[ApplicationReadyEvent]: app is ready")
    }
}

fun main(args: Array<String>) {
    SpringApplicationBuilder(SpringKotlinJwtBoilerplateApplication::class.java)
        .bannerMode(Banner.Mode.CONSOLE)
        .logStartupInfo(true)
        .listeners(ApplicationListener {
                event: ApplicationEvent -> getLogger().info("#### event> ${event.javaClass.canonicalName}")
        })
        .build()
        .run(*args)
    //AppConstants.setLevel("debug")
}
