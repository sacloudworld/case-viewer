package com.example.case_viewer.config;

import com.example.case_viewer.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    /**
     * Configures Spring Security for the application.
     *
     * <p>
     * The security configuration:
     * <ul>
     * <li>Disables CSRF protection.</li>
     * <li>Disables form-based login.</li>
     * <li>Disables HTTP Basic authentication.</li>
     * <li>Allows unauthenticated access to Swagger and Prometheus endpoints.</li>
     * <li>Allows unauthenticated access to registration and login APIs.</li>
     * <li>Requires authentication for case APIs.</li>
     * <li>Requires authentication for all other endpoints by default.</li>
     * <li>Adds the custom JWT authentication filter before
     * {@link UsernamePasswordAuthenticationFilter}.</li>
     * </ul>
     *
     * @param http the Spring Security {@link HttpSecurity} configuration object
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                /**
                 * Disable CSRF protection.
                 *
                 * CSRF is primarily important for browser-based applications
                 * that use cookies/session authentication.
                 *
                 * For a stateless REST API using JWT in the Authorization header,
                 * CSRF protection is generally not required.
                 */
                .csrf(csrf -> csrf.disable())

                /**
                 * Disable Spring Security's default HTML login form.
                 *
                 * The application uses JWT authentication instead of
                 * browser-based form login.
                 */
                .formLogin(form -> form.disable())

                /**
                 * Disable HTTP Basic authentication.
                 *
                 * The client will authenticate using a JWT rather than:
                 *
                 * Authorization: Basic username:password
                 */
                .httpBasic(basic -> basic.disable())

                /**
                 * Configure authorization rules for HTTP requests.
                 */
                .authorizeHttpRequests(auth -> auth

                        /**
                         * Swagger/OpenAPI and Prometheus endpoints are publicly accessible.
                         *
                         * No JWT authentication is required for these endpoints.
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/prometheus",
                                 "/actuator/metrics/**",
                                    "/actuator/caches")
                        .permitAll()

                        /**
                         * Authentication and registration endpoints are public.
                         *
                         * A user cannot be expected to provide a JWT
                         * before logging in or registering.
                         */
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/error")
                        .permitAll()

                        /**
                         * The React UI (built into resources/static) is public;
                         * it calls the JWT-protected APIs itself.
                         */
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/agent",
                                "/agent/",
                                "/agent/index.html",
                                "/assets/**",
                                "/favicon.svg")
                        .permitAll()

                        /**
                         * Agent console APIs, and the JDBC endpoints that list
                         * every customer's cases, are for agents only.
                         */
                        .requestMatchers(
                                "/api/agent/**",
                                "/api/cases/search/jdbc/**",
                                "/api/cases/jdbc/**")
                        .hasRole("AGENT")

                        /**
                         * The customer secure inbox is for customers only.
                         */
                        .requestMatchers("/api/inbox/**")
                        .hasRole("USER")

                        /**
                         * All Case APIs require an authenticated user.
                         *
                         * Example:
                         *
                         * GET /api/cases/123
                         */
                        .requestMatchers("/api/cases/**")
                        .authenticated()

                        /**
                         * Default rule.
                         *
                         * Any endpoint that was not explicitly permitted above
                         * also requires authentication.
                         */
                        .anyRequest()
                        .authenticated());

        /**
         * Register the custom JWT authentication filter.
         *
         * The JWT filter executes before Spring Security's
         * UsernamePasswordAuthenticationFilter.
         *
         * The JWT filter typically:
         *
         * 1. Reads the Authorization header.
         * 2. Extracts the JWT.
         * 3. Validates the JWT.
         * 4. Extracts the user's identity/roles.
         * 5. Creates an Authentication object.
         * 6. Stores the Authentication in SecurityContext.
         *
         * This allows subsequent Spring Security authorization
         * rules to know that the request is authenticated.
         */
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        /**
         * Build and return the SecurityFilterChain.
         */
        return http.build();
    }
}