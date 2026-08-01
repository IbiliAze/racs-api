package uk.co.eightmile.racs.common.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String query = request.getQueryString();
            String uri = query != null
                    ? request.getRequestURI() + '?' + query
                    : request.getRequestURI();

            if (status >= 500) {
                log.error("{} {} -> {} ({} ms)", request.getMethod(), uri, status, duration);
            } else if (status >= 400) {
                log.warn("{} {} -> {} ({} ms)", request.getMethod(), uri, status, duration);
            } else {
                log.info("{} {} -> {} ({} ms)", request.getMethod(), uri, status, duration);
            }
        }
    }
}