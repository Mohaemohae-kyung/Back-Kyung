package kyung.kung_backend.global.lab;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class ExpertProfileUploadJspAccessBlockFilter {

    private static final String UPLOAD_PATH_PREFIX = "/uploads/expert-profile/";

    @Bean
    public FilterRegistrationBean<Filter> expertProfileUploadJspBlockRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();

        registration.setFilter(this::blockJspAccess);
        registration.addUrlPatterns("/uploads/expert-profile/*");
        registration.setName("expertProfileUploadJspBlockFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }

    private void blockJspAccess(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String normalizedPath = normalizeRequestPath(httpRequest.getRequestURI());

        if (normalizedPath.startsWith(UPLOAD_PATH_PREFIX) && normalizedPath.contains(".jsp")) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private String normalizeRequestPath(String value) {
        String normalized = value == null ? "" : value;

        for (int i = 0; i < 3; i++) {
            String decoded = URLDecoder.decode(normalized, StandardCharsets.UTF_8);
            if (decoded.equals(normalized)) {
                break;
            }

            normalized = decoded;
        }

        return normalized
                .replace('\\', '/')
                .toLowerCase(Locale.ROOT);
    }
}
