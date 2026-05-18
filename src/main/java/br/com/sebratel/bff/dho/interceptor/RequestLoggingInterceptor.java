package br.com.sebratel.bff.dho.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String userEmail = "Anonymous";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            userEmail = authentication.getName();
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        log.info("Request Processed - User: {}, Route: {} {}, Status: {}", userEmail, method, uri, status);
        
        if (ex != null) {
            log.error("Request Exception - User: {}, Route: {} {}, Exception: {}", userEmail, method, uri, ex.getMessage());
        }
    }
}
