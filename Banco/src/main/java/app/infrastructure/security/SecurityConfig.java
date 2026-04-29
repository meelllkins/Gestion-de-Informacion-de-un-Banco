package app.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                    // ── Públicos ──────────────────────────────────────
                    .requestMatchers("/auth/**").permitAll()

                    // ── Registro de clientes (público) ────────────────
                    .requestMatchers(HttpMethod.POST, "/api/customers/individual/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/customers/corporate/register").permitAll()

                    // ── Registro de empleados (solo INTERNAL_ANALYST) ─
                    .requestMatchers(HttpMethod.POST, "/api/employees/**")
                            .hasRole("INTERNAL_ANALYST")

                    // ── Operaciones de ventanilla ─────────────────────
                    .requestMatchers("/api/accounts/open").hasRole("TELLER_EMPLOYEE")
                    .requestMatchers("/api/accounts/deposit").hasRole("TELLER_EMPLOYEE")
                    .requestMatchers("/api/accounts/withdraw").hasRole("TELLER_EMPLOYEE")

                    // ── Consulta de cuentas ───────────────────────────
                    .requestMatchers(HttpMethod.GET, "/api/accounts/**")
                            .hasAnyRole("TELLER_EMPLOYEE", "COMMERCIAL_EMPLOYEE",
                                        "INDIVIDUAL_CUSTOMER", "CORPORATE_CUSTOMER",
                                        "CORPORATE_EMPLOYEE", "CORPORATE_SUPERVISOR",
                                        "INTERNAL_ANALYST")

                    // ── Préstamos: solicitud ──────────────────────────
                    .requestMatchers(HttpMethod.POST, "/api/loans/request")
                            .hasAnyRole("INDIVIDUAL_CUSTOMER", "CORPORATE_CUSTOMER",
                                        "COMMERCIAL_EMPLOYEE")

                    // ── Préstamos: aprobación/rechazo/desembolso ──────
                    .requestMatchers("/api/loans/approve/**").hasRole("INTERNAL_ANALYST")
                    .requestMatchers("/api/loans/reject/**").hasRole("INTERNAL_ANALYST")
                    .requestMatchers("/api/loans/disburse/**").hasRole("INTERNAL_ANALYST")

                    // ── Préstamos: consulta ───────────────────────────
                    .requestMatchers(HttpMethod.GET, "/api/loans/**")
                            .hasAnyRole("INDIVIDUAL_CUSTOMER", "CORPORATE_CUSTOMER",
                                        "COMMERCIAL_EMPLOYEE", "INTERNAL_ANALYST")

                    // ── Transferencias: creación ──────────────────────
                    .requestMatchers(HttpMethod.POST, "/api/transfers/**")
                            .hasAnyRole("INDIVIDUAL_CUSTOMER", "CORPORATE_CUSTOMER",
                                        "CORPORATE_EMPLOYEE")

                    // ── Transferencias: aprobación/rechazo ────────────
                    .requestMatchers("/api/transfers/approve/**")
                            .hasRole("CORPORATE_SUPERVISOR")
                    .requestMatchers("/api/transfers/reject/**")
                            .hasRole("CORPORATE_SUPERVISOR")

                    // ── Transferencias: consulta ──────────────────────
                    .requestMatchers(HttpMethod.GET, "/api/transfers/**")
                            .hasAnyRole("INDIVIDUAL_CUSTOMER", "CORPORATE_CUSTOMER",
                                        "CORPORATE_EMPLOYEE", "CORPORATE_SUPERVISOR",
                                        "INTERNAL_ANALYST")

                    // ── Bitácora: solo analista interno ───────────────
                    .requestMatchers("/api/logs/**").hasRole("INTERNAL_ANALYST")

                    // ── Todo lo demás requiere autenticación ──────────
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                                "{\"status\":401,\"message\":\"No autenticado: se requiere un token válido\",\"errors\":null}");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                                "{\"status\":403,\"message\":\"Acceso denegado: no tiene permisos para este recurso\",\"errors\":null}");
                    })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}