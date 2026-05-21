package com.indramind.cybersec.secure_tasks_api.security;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@SuppressFBWarnings(
            value = "SERVLET_HEADER",
            justification = "Correlation Id is sanitized and validated all it can be validated." // because false positives were appearing even though CRLF chars are escaped
        )
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-ID";

    public static final String CORRELATION_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = sanitizeCorrelationId(request.getHeader(HEADER));

        MDC.put(CORRELATION_KEY, correlationId);
        response.setHeader(HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String sanitizeCorrelationId(String correlationId){ // to prevent things like 5MB long correlation id headers to fill the logs
        if (correlationId == null || correlationId.isBlank()) { // if not set -> generate random UUID
            return UUID.randomUUID().toString();
        }

        try {
            return UUID.fromString(correlationId.trim()).toString();
        } catch (IllegalArgumentException e) {
            return UUID.randomUUID().toString(); // If not in UUID format --> generate random UUID
        }
    }
}
