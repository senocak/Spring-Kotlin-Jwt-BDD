package com.github.senocak.boilerplate.util.validation

import com.github.senocak.boilerplate.util.logger
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.Logger
import java.util.regex.Pattern
import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(allowedTargets = [AnnotationTarget.FIELD])
@Retention(value = AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EmailValidator::class])
annotation class ValidEmail (
    val message: String = "Invalid email",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class EmailValidator: ConstraintValidator<ValidEmail?, String?> {
    private val log: Logger by logger()

    override fun initialize(constraintAnnotation: ValidEmail?) {
        log.info("EmailValidator initialized")
    }

    override fun isValid(email: String?, context: ConstraintValidatorContext): Boolean {
        if (email == null)
            return false
        val pattern: Pattern = Pattern.compile(
            "^[_A-Za-z0-9-+]" +
                    "(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*" + "(.[A-Za-z]{2,})$"
        )
        return pattern
            .matcher(email)
            .matches()
    }
}

