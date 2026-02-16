package com.fallapp.config;

import com.fallapp.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad de Spring Security con JWT
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el AuthenticationManager como bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sin autenticación)
                .requestMatchers(
                    "/api/auth/**",
                    "/api/test-email/**",  // Endpoints de prueba de email
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Endpoints de solo lectura públicos (exploración sin login)
                .requestMatchers(HttpMethod.GET, "/api/fallas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/eventos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/comentarios/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/estadisticas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").permitAll()
                
                // Endpoints de creación (requieren autenticación, cualquier usuario)
                .requestMatchers(HttpMethod.POST, "/api/fallas").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/eventos").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/comentarios").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/votos").authenticated()
                
                // Endpoints de actualización (requieren autenticación)
                .requestMatchers(HttpMethod.PUT, "/api/fallas/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/eventos/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/comentarios/**").authenticated()
                
                // Endpoints de eliminación (solo ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/fallas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/comentarios/**").hasRole("ADMIN")
                
                // Gestión de usuarios (requiere autenticación)
                .requestMatchers("/api/usuarios/**").authenticated()
                
                // Por defecto, requiere autenticación
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8081",
            "http://35.180.21.42:8080",
            "http://35.180.21.42:3000",
            "http://35.180.21.42:5173"
        ));
        // Permitir todos los orígenes en desarrollo (comentar en producción)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
