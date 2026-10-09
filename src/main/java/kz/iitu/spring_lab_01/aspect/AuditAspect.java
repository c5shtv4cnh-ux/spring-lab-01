package kz.iitu.spring_lab_01.aspect;

import kz.iitu.spring_lab_01.audit.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Order(1)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        String action = audited.action();

        if (audited.logArguments()) {
            log.info("[AUDIT] start {} at {} args={}",
                    action, LocalDateTime.now(), Arrays.toString(pjp.getArgs()));
        } else {
            log.info("[AUDIT] start {} at {}", action, LocalDateTime.now());
        }

        try {
            Object result = pjp.proceed();
            log.info("[AUDIT] {} success at {}", action, LocalDateTime.now());
            return result;
        } catch (Throwable ex) {
            log.info("[AUDIT] {} failure at {}: {}", action, LocalDateTime.now(), ex.getMessage());
            throw ex;
        }
    }
}