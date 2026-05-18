package kyung.kung_backend.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 기본 설정을 담당하는 설정 클래스입니다.
 *
 * 왜 필요한지:
 * - build.gradle에 spring-boot-starter-security가 포함되어 있으면 Spring Boot가 기본 보안 정책을 자동 적용합니다.
 * - 별도 설정이 없으면 대부분의 API가 인증을 요구하고, POST/PUT/PATCH/DELETE 요청은 CSRF 검증도 받습니다.
 * - 그래서 Swagger에서 POST /api/payments/prepare를 호출하면 Controller까지 도달하기 전에 403 Forbidden이 발생합니다.
 *
 * 현재 역할:
 * - Swagger 문서 페이지 접근을 허용합니다.
 * - 결제 준비 API 스켈레톤 테스트를 위해 POST /api/payments/prepare만 임시로 허용합니다.
 * - REST API 방식에 맞게 CSRF를 비활성화합니다.
 * - 프론트엔드 로컬 개발 서버에서 호출할 수 있도록 CORS 기본값을 둡니다.
 *
 * 이후 JWT 로그인 기능이 구현되면:
 * - /api/payments/prepare permitAll 설정을 제거합니다.
 * - JWT 인증 필터를 SecurityFilterChain에 추가합니다.
 * - PaymentService.prepare()에서 현재 로그인 사용자와 결제 대상 사용자가 같은지 검증합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * HTTP 요청별 보안 규칙을 정의합니다.
     *
     * 호출되는 시점:
     * - 애플리케이션 시작 시 Spring이 이 Bean을 읽어 SecurityFilterChain을 생성합니다.
     * - 모든 HTTP 요청은 Controller에 도달하기 전에 이 필터 체인을 먼저 통과합니다.
     *
     * 현재 허용한 경로:
     * - /swagger, /swagger-ui/**, /v3/api-docs/**: Swagger 화면과 OpenAPI 문서 로딩용
     * - POST /api/payments/prepare: 결제 준비 API 스켈레톤 확인용
     * - OPTIONS /**: 브라우저 CORS preflight 요청용
     * - /error: Controller 또는 Service 예외가 발생했을 때 Spring Boot 기본 에러 처리 경로가 막히지 않도록 허용
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/prepare").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * CORS 설정입니다.
     *
     * 사용하는 곳:
     * - securityFilterChain()의 .cors(Customizer.withDefaults())가 이 Bean을 찾아 사용합니다.
     * - 브라우저에서 localhost:3000 또는 localhost:5173 프론트엔드가 백엔드 API를 호출할 때 적용됩니다.
     *
     * Swagger는 같은 서버 localhost:8080에서 열리므로 CORS가 직접 원인은 아니지만,
     * 프론트엔드 연결 시 바로 테스트할 수 있도록 함께 둡니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
