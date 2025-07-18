package com.github.senocak.boilerplate.util.validation

import com.github.senocak.boilerplate.domain.dto.UpdateUserDto
import com.github.senocak.boilerplate.util.logger
import org.slf4j.Logger
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.getValue
import kotlin.reflect.KClass

@Target(allowedTargets = [AnnotationTarget.CLASS])
@Retention(value = AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordMatchesValidator::class])
annotation class PasswordMatches(
    val message: String = "Passwords don''t match",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class PasswordMatchesValidator: ConstraintValidator<PasswordMatches, Any> {
    private val log: Logger by logger()

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
