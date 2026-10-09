package kz.iitu.spring_lab_01.aspect;

import jakarta.servlet.http.HttpServletRequest;
import kz.iitu.spring_lab_01.security.AccessDeniedException;
import kz.iitu.spring_lab_01.security.RequiresRole;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AccessControlAspect {

    private static final Logger log = LoggerFactory.getLogger(AccessControlAspect.class);
    private static final String ROLE_HEADER = "X-User-Role";

    @Before("@annotation(requiresRole)")
    public void checkAccess(RequiresRole requiresRole) {
        String requiredRole = requiresRole.value();
        String actualRole = getRoleFromRequest();

        log.info("[ACCESS] required={} actual={}", requiredRole, actualRole);

        // ДОПИШИТЕ САМИ: если actualRole не равна requiredRole (в том числе когда actualRole == null),
        if (!requiredRole.equals(actualRole)) {
            throw new AccessDeniedException("Access denied: required role " + requiredRole + ", but got " + actualRole);
        }
        // выбросьте new AccessDeniedException("...сообщение...").
    }

    private String getRoleFromRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader(ROLE_HEADER);
    }
}