package com.pms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class WebConfig {

    public static Long getCurrentUserId() {
        Object uid = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            if (request != null) uid = request.getAttribute("userId");
        }
        if (uid == null && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Number) uid = principal;
        }
        if (uid instanceof Long) return (Long) uid;
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Number) return ((Number) uid).longValue();
        return null;
    }
}
