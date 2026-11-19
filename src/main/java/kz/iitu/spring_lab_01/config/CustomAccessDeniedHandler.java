package kz.iitu.spring_lab_01.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Individual variant 3: our own reaction to "authenticated, but not allowed".
 * Called by ExceptionTranslationFilter (and by CsrfFilter) instead of the default handler;
 * keeps status 403 and forwards to our template instead of the Whitelabel error page.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    public static final String DENIED_PATH = "/access-denied";
    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String who = auth != null ? auth.getName() : "anonymous";
        log.warn("[ACCESS DENIED] user={} {} {} : {}", who, request.getMethod(), request.getRequestURI(), ex.getMessage());

        request.setAttribute("deniedPath", request.getRequestURI());
        request.setAttribute("deniedMethod", request.getMethod());
        request.setAttribute("deniedReason", ex.getClass().getSimpleName());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher(DENIED_PATH).forward(request, response);
    }
}
