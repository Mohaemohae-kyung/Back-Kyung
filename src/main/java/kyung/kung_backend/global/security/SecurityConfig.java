package kyung.kung_backend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import kyung.kung_backend.global.jwt.JwtAuthenticationFilter;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] HEALTH_WHITE_LIST = {
            "/health"
    };

    private static final String[] SWAGGER_WHITE_LIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    private static final String[] AUTH_WHITE_LIST = {
            "/api/auth/**",
            "/ws-stomp/**",
            "/ws-stomp-android/**",
            "/api/chat/llm",
            "/api/payments/internal/**"
    };

    // Swagger는 운영 환경에서 외부 전체 공개하지 않고,
    // 서버 내부 또는 사설망에서만 접근할 수 있도록 제한합니다.
    private static final List<IpAddressMatcher> INTERNAL_IP_MATCHERS = List.of(
            new IpAddressMatcher("127.0.0.1"),
            new IpAddressMatcher("::1"),
            new IpAddressMatcher("10.0.0.0/8"),
            new IpAddressMatcher("172.16.0.0/12"),
            new IpAddressMatcher("192.168.0.0/16")
    );

    private static final String[] APP_INTEGRITY_WHITE_LIST = {
            "/api/app-integrity/report"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // REST API + JWT 예정 구조이므로 CSRF는 비활성화합니다.
                .csrf(AbstractHttpConfigurer::disable)

                // 프론트엔드에서 백엔드 API 호출을 허용하기 위한 CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // JWT 기반 인증을 사용하므로 세션을 생성하지 않습니다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 기본 로그인 폼은 사용하지 않습니다.
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증은 사용하지 않습니다.
                .httpBasic(AbstractHttpConfigurer::disable)

                // Spring Security Filter 단계에서 발생하는 인증/인가 예외를 JSON으로 처리합니다.
                .exceptionHandling(exception -> exception

                        // 인증 실패
                        .authenticationEntryPoint((request, response, authException) -> {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            response.setContentType("application/json;charset=UTF-8");

                            ApiResponse<?> body =
                                    ApiResponse.onFailure(
                                            ErrorCode.UNAUTHORIZED,
                                            "인증이 필요합니다."
                                    );

                            response.getWriter().write(
                                    objectMapper.writeValueAsString(body)
                            );
                        })

                        // 권한 부족
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                            response.setContentType("application/json;charset=UTF-8");

                            ApiResponse<?> body =
                                    ApiResponse.onFailure(
                                            ErrorCode.FORBIDDEN,
                                            "접근 권한이 없습니다."
                                    );

                            response.getWriter().write(
                                    objectMapper.writeValueAsString(body)
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(
                                DispatcherType.FORWARD,
                                DispatcherType.ERROR
                        ).permitAll()

                        .requestMatchers("/error").permitAll()

                        .requestMatchers(HttpMethod.GET, "/uploads/expert-profile/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/lab/jsp/status").permitAll()

                        .requestMatchers(HttpMethod.GET, "/lab/jsp/**").permitAll()

                        // health check
                        .requestMatchers(HEALTH_WHITE_LIST).permitAll()

                        // Swagger 문서 경로는 외부 전체 공개하지 않고 내부 IP에서만 허용합니다.
                        .requestMatchers(SWAGGER_WHITE_LIST).access(this::internalOnly)

                        // 회원가입, 로그인 등 인증 전 접근이 필요한 API
                        .requestMatchers(AUTH_WHITE_LIST).permitAll()

                        // 조회는 로그인 없이 허용
                        .requestMatchers(HttpMethod.GET, "/api/experts/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/expert-services/**").permitAll()

                        .requestMatchers("/api/portfolios/viewer").permitAll()

                        // 고수 서비스 등록은 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/api/expert-services/**").authenticated()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/store-products/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/community/**").permitAll()

                        // 파일 업로드는 로그인 사용자만
                        .requestMatchers(HttpMethod.POST, "/api/files").authenticated()

                        // 요청관리
                        .requestMatchers("/api/service-requests/**").authenticated()

                        // 앱 무결성 api
                        .requestMatchers(APP_INTEGRITY_WHITE_LIST).permitAll()

                        .requestMatchers("/seal-authz.txt", "/seal-authz").permitAll()

                        // 관리자 API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 등록
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 로컬 프론트엔드 허용
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://can-fly.shop",
                "https://www.can-fly.shop",

                // 모든 Vercel 배포 프론트엔드 허용 고태완식 해결법
                "https://*.vercel.app"
        ));

        // 허용 메서드
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // 허용 헤더
        config.setAllowedHeaders(List.of("*"));

        // Authorization / 쿠키 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private AuthorizationDecision internalOnly(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context
    ) {
        String remoteAddress = context.getRequest().getRemoteAddr();

        boolean isInternalIp = INTERNAL_IP_MATCHERS.stream()
                .anyMatch(matcher -> matcher.matches(remoteAddress));

        return new AuthorizationDecision(isInternalIp);
    }
}
