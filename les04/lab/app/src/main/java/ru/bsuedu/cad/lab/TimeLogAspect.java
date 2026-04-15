package ru.bsuedu.cad.lab;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TimeLogAspect {

    @Around("within(ru.bsuedu.cad.lab.CSVParser)")
    private Object logRimePointcut(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj = pjp.proceed();
        long end = System.currentTimeMillis();
        System.out.println("Parsing time is millis: "  + (end - start));
        return obj;
    }
}
