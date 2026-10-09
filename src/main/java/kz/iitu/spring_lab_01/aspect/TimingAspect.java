package kz.iitu.spring_lab_01.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(3)
public class TimingAspect {

    private static final Logger log = LoggerFactory.getLogger(TimingAspect.class);

    @Around("kz.iitu.spring_lab_01.aspect.Pointcuts.serviceOperation()")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        long started = System.nanoTime();
        try {
            return pjp.proceed(); // без этой строки целевой метод не выполнится
        } finally {
            long ms = (System.nanoTime() - started) / 1_000_000;
            String name = pjp.getSignature().toShortString();
            if (ms > 200) {
                log.warn("[TIME] SLOW: {} — {} ms", name, ms);
            } else {
                log.info("[TIME] {} — {} ms", name, ms);
            }
        }
    }
}