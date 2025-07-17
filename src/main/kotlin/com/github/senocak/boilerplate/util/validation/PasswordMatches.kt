package com.github.senocak.boilerplate.util.validation

import com.github.senocak.boilerplate.domain.dto.UpdateUserDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordMatchesValidator::class])
annotation class PasswordMatches(
    val message: String = "Passwords don''t match",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class PasswordMatchesValidator : ConstraintValidator<PasswordMatches, Any> {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun initialize(passwordMatches: PasswordMatches) {
        log.info("PasswordMatchesValidator initialized")
    }

    override fun isValid(obj: Any, context: ConstraintValidatorContext): Boolean {
        if (obj.javaClass == UpdateUserDto::class.java) {
            val (_, password, password_confirmation) = obj as UpdateUserDto
            return password == password_confirmation
        }
        return false
    }
}
