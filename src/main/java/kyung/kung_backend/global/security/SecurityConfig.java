package kyung.kung_backend.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITE_LIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    private static final String[] AUTH_WHITE_LIST = {
            "/api/auth/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API + JWT 예정 구조이므로 CSRF는 비활성화합니다.
                .csrf(AbstractHttpConfigurer::disable)

                // 추후 JWT 기반 인증을 사용할 예정이므로 세션을 생성하지 않습니다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 기본 로그인 폼은 사용하지 않습니다.
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증은 사용하지 않습니다.
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Swagger 문서 확인용 경로
                        .requestMatchers(SWAGGER_WHITE_LIST).permitAll()

                        // 회원가입, 로그인 등 인증 전 접근이 필요한 API
                        .requestMatchers(AUTH_WHITE_LIST).permitAll()

                        // JWT 구현 전까지는 개발 편의를 위해 전체 API를 임시 허용합니다.
                        // 추후 JWT 필터 추가 후 authenticated()로 변경합니다.
                        .anyRequest().permitAll()
                );

                /*
                // 추후 JWT 필터 구현 후 추가 예정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                */

        return http.build();
    }

    /*
    // 추후 회원가입/로그인에서 비밀번호 암호화가 필요할 때 추가 예정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    */

    /*
    // 추후 프론트엔드 연동 시 CORS 설정이 필요하면 추가 예정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000"
                // "https://추후-배포-프론트-URL"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    */

    /*
    // 추후 @PreAuthorize, @PostAuthorize 등 메서드 단위 권한 제어가 필요하면
    // 클래스 상단에 아래 어노테이션 추가 예정
    //
    // @EnableMethodSecurity
    */

    /*
    // 추후 JWT 인증 실패 응답을 ApiResponse 형식으로 통일할 때 exceptionHandling 추가 예정
    //
    // .exceptionHandling(exception -> exception
    //         .authenticationEntryPoint((request, response, authException) -> {
    //             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    //             response.setContentType("application/json;charset=UTF-8");
    //             response.getWriter().write(
    //                     "{\"isSuccess\":false,\"code\":\"AUTH_401\",\"message\":\"인증이 필요합니다.\"}"
    //             );
    //         })
    // )
    */
}