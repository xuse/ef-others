package org.easyframe.enterprise.spring.support;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * create a pointcut to log time
 */
@Aspect
public class LogAspect {
	public LogAspect(){
		System.out.println("=-==========================================");
	}
	
	
    @Around("(execution (public * org.easyframe.enterprise.spring.BaseDao+.*(..)))")
    public Object traceMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start=System.currentTimeMillis();
        try {
            return  proceedingJoinPoint.proceed();
        } finally {
        	long cost=System.currentTimeMillis()-start;
        	String methodName=proceedingJoinPoint.getSignature().toShortString();
        	getLog(proceedingJoinPoint).info("[Timming] Method [{}] executed within {}ms.",methodName, cost);
        }
    }

    protected Logger getLog(final JoinPoint joinPoint) {
        final Object target = joinPoint.getTarget();
        if (target != null) {
            return LoggerFactory.getLogger(target.getClass());
        }
        return LoggerFactory.getLogger(getClass());
    }
}