package com.maids.lms.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut for all service methods
     */
    @Pointcut("execution(* com.maids.lms.service.*.*(..))")
    public void serviceLayer() {}

    /**
     * Log method calls with performance metrics
     */
    @Around("serviceLayer()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.debug("→ [{}.{}] called with args: {}", className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("← [{}.{}] completed in {}ms", className, methodName, elapsed);
            if (elapsed > 1000) {
                log.warn("⚠ SLOW METHOD: [{}.{}] took {}ms", className, methodName, elapsed);
            }
            return result;
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("✗ [{}.{}] threw {} after {}ms: {}",
                    className, methodName, ex.getClass().getSimpleName(), elapsed, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Log book additions
     */
    @AfterReturning(pointcut = "execution(* com.maids.lms.service.BookService.addBook(..))", returning = "result")
    public void logBookAdded(JoinPoint joinPoint, Object result) {
        log.info("📚 Book added: {}", result);
    }

    /**
     * Log book updates
     */
    @AfterReturning(pointcut = "execution(* com.maids.lms.service.BookService.updateBook(..))", returning = "result")
    public void logBookUpdated(JoinPoint joinPoint, Object result) {
        log.info("📝 Book updated: {}", result);
    }

    /**
     * Log patron transactions (borrow/return)
     */
    @AfterReturning(pointcut = "execution(* com.maids.lms.service.BorrowingService.borrowBook(..))", returning = "result")
    public void logBookBorrowed(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        log.info("📤 Book borrowed - bookId: {}, patronId: {}, record: {}", args[0], args[1], result);
    }

    @AfterReturning(pointcut = "execution(* com.maids.lms.service.BorrowingService.returnBook(..))", returning = "result")
    public void logBookReturned(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        log.info("📥 Book returned - bookId: {}, patronId: {}, record: {}", args[0], args[1], result);
    }

    /**
     * Log exceptions from any controller
     */
    @AfterThrowing(pointcut = "execution(* com.maids.lms.controller.*.*(..))", throwing = "ex")
    public void logControllerException(JoinPoint joinPoint, Exception ex) {
        log.error("Controller exception in [{}.{}]: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }
}
