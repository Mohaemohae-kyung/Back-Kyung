package kyung.kung_backend.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("========== JWT FILTER ==========");
        System.out.println("REQUEST URI = " + request.getRequestURI());

        String token = resolveToken(request);

        System.out.println("TOKEN = " + token);

        if (token != null) {

            boolean valid = jwtProvider.validateToken(token);

            System.out.println("TOKEN VALID = " + valid);

            if (valid) {

                Long userId = jwtProvider.getUserId(token);

                System.out.println("USER ID = " + userId);

                User user = userRepository.findById(userId).orElse(null);

                System.out.println("USER = " + user);

                if (user != null) {

                    System.out.println("USER STATUS = " + user.getStatus());
                    System.out.println("USER ROLE = " + user.getRole());

                    if ("ACTIVE".equals(user.getStatus())) {

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        List.of(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_" + user.getRole()
                                                )
                                        )
                                );

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(authentication);

                        System.out.println("AUTHENTICATION SUCCESS");
                    } else {
                        System.out.println("USER STATUS NOT ACTIVE");
                    }

                } else {
                    System.out.println("USER NOT FOUND");
                }

            } else {
                System.out.println("TOKEN INVALID");
            }

        } else {
            System.out.println("TOKEN NULL");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {

        String authorization = request.getHeader("Authorization");

        System.out.println("AUTH HEADER = " + authorization);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        return null;
    }
}