package com.github.senocak.boilerplate.security

import com.github.senocak.boilerplate.service.AuthenticationService
import com.github.senocak.boilerplate.util.logger
import org.slf4j.Logger
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor
import java.security.InvalidParameterException
import java.util.Collections
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class AuthorizationInterceptor(private val authenticationService: AuthenticationService): AsyncHandlerInterceptor {
    private val log: Logger by logger()

    /**
     * Interception point before the execution of a handler.
     * @param request -- Request information for HTTP servlets.
     * @param response -- It is where the servlet can write information about the data it will send back.
     * @param handler -- Class Object is the root of the class hierarchy.
     * @return -- true or false or AccessDeniedException
     */
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val handlerMethod: HandlerMethod = try {
            handler as HandlerMethod
        } catch (e: ClassCastException) {
            return true
        }
        validateQueryParams(request = request, handler = handlerMethod)
        val authorizeAnnotation: Authorize? = getAuthorizeAnnotation(handlerMethod = handlerMethod)
        if (authorizeAnnotation != null && !hasAnnotationRole(authorize = authorizeAnnotation)) {
            log.error("Throwing AccessDeniedException because role is not valid for api")
            throw AccessDeniedException("You are not allowed to perform this operation")
        }
        return true
    }

    /**
     * Validation of the request params to check unhandled ones
     * @param request -- Request information for HTTP servlets.
     * @param handler -- Encapsulates information about a handler method consisting of a method
     */
    private fun validateQueryParams(request: HttpServletRequest, handler: HandlerMethod) {
        val queryParams: MutableList<String> = Collections.list(request.parameterNames)
        val methodParameters = handler.methodParameters
        val expectedParams: MutableList<String> = ArrayList(methodParameters.size)
        for (methodParameter in methodParameters) {
            val requestParam = methodParameter.getParameterAnnotation(RequestParam::class.java)
            if (requestParam != null) {
                if (StringUtils.hasText(requestParam.name))
                    expectedParams.add(element = requestParam.name)
                else {
                    methodParameter.initParameterNameDiscovery(DefaultParameterNameDiscoverer())
                    expectedParams.add(element = methodParameter.parameterName!!)
                }
            }
        }
        queryParams.removeAll(elements = expectedParams)
        if (queryParams.isNotEmpty()) {
            log.error("Unexpected parameters: $queryParams")
            throw InvalidParameterException("unexpected parameter: $queryParams")
        }
    }

    /**
     * Get infos for Authorize annotation that defined for class or method
     * @param handlerMethod -- RequestMapping method that reached to server
     * @return -- Authorize annotation or null
     */
    private fun getAuthorizeAnnotation(handlerMethod: HandlerMethod): Authorize? {
        if (handlerMethod.method.declaringClass.isAnnotationPresent(Authorize::class.java))
            return handlerMethod.method.declaringClass.getAnnotation(Authorize::class.java)
        if (handlerMethod.method.isAnnotationPresent(Authorize::class.java))
            return handlerMethod.method.getAnnotation(Authorize::class.java)
        if (handlerMethod.method.javaClass.isAnnotationPresent(Authorize::class.java))
            return handlerMethod.method.javaClass.getAnnotation(Authorize::class.java)
        return null
    }

    /**
     * Checks the roles of user for defined Authorize annotation
     * @param authorize - parameter that has roles
     * @return -- false if not authorized
     * @throws BadCredentialsException -- throws BadCredentialsException
     * @throws AccessDeniedException -- throws AccessDeniedException
     */
    @Throws(BadCredentialsException::class, AccessDeniedException::class)
    private fun hasAnnotationRole(authorize: Authorize): Boolean {
        return try {
            authenticationService.isAuthorized(aInRoles = authorize.roles)
        } catch (ex: Exception) {
            log.trace("Exception occurred while authorizing. Exception: ${ex.localizedMessage}")
            false
        }
    }
}
