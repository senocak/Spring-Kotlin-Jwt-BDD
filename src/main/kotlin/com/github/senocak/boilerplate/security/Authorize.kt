package com.github.senocak.boilerplate.security

@Target(allowedTargets = [
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
])
@Retention(value = AnnotationRetention.RUNTIME)
annotation class Authorize(val roles: Array<String>)
