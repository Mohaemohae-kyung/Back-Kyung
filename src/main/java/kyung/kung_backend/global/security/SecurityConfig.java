package kyung.kung_backend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import kyung.kung_backend.global.jwt.JwtAuthenticationFilter;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
            "/ws-stomp/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API + JWT 인증 구조이므로 CSRF는 비활성화합니다.
                .csrf(AbstractHttpConfigurer::disable)

                // JWT 기반 인증을 사용하므로 서버 세션을 생성하지 않습니다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 기본 로그인 폼은 사용하지 않습니다.
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증은 사용하지 않습니다.
                .httpBasic(AbstractHttpConfigurer::disable)

                // Spring Security Filter 단계에서 발생하는 인증/인가 예외를 JSON으로 처리합니다.
                .exceptionHandling(exception -> exception
                        // 인증 실패: 토큰 없음, 토큰 만료, 토큰 이상 등
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");

                            ApiResponse<?> body = ApiResponse.onFailure(
                                    ErrorCode.UNAUTHORIZED,
                                    "인증이 필요합니다."
                            );

                            response.getWriter().write(objectMapper.writeValueAsString(body));
                        })

                        // 인가 실패: 로그인은 했지만 권한이 부족한 경우
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");

                            ApiResponse<?> body = ApiResponse.onFailure(
                                    ErrorCode.FORBIDDEN,
                                    "접근 권한이 없습니다."
                            );

                            response.getWriter().write(objectMapper.writeValueAsString(body));
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // health check 확인용 경로
                        .requestMatchers(HEALTH_WHITE_LIST).permitAll()

                        // Swagger 문서 확인용 경로
                        .requestMatchers(SWAGGER_WHITE_LIST).permitAll()

                        // 회원가입, 로그인 등 인증 전 접근이 필요한 API
                        .requestMatchers(AUTH_WHITE_LIST).permitAll()

                        // 관리자 전용 API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 JWT 인증을 요구합니다.
                        .anyRequest().authenticated()
                )

                // 요청의 Authorization 헤더에서 JWT를 검증하고 인증 객체를 주입합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 회원가입/로그인 시 비밀번호 암호화를 위해 사용합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}