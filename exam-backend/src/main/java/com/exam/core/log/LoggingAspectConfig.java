package com.exam.core.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspectConfig {
    @Pointcut("@annotation(com.exam.core.log.Loggable)")
    public void loggableMethods() {
        // Pointcut for methods annotated with @Loggable
    }

    @Pointcut("within(@org.springframework.stereotype.Repository *) || " +
            "within(@org.springframework.stereotype.Service *) || " +
            "within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // Pointcut that matches all repositories, services, and Web REST endpoints
    }

    @Pointcut("within(com.exam..*)")
    public void applicationPackagePointcut() {
        // Pointcut that matches all Spring beans in the application's main packages
    }

    @Pointcut("(applicationPackagePointcut() && springBeanPointcut()) || loggableMethods()")
    public void loggableOperations() {
    }

    @Pointcut("@annotation(com.exam.core.log.NoLogging) || @within(com.exam.core.log.NoLogging)")
    public void noLoggingOperations() {
    }

    @Around("loggableOperations() && !noLoggingOperations()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Enter: {}.{}() with argument[s] = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                joinPoint.getArgs());

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsedTime = System.currentTimeMillis() - start;

        log.debug("Exit: {}.{}() with result = {}. Executed in {} ms",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                result, elapsedTime);
        return result;
    }
}
