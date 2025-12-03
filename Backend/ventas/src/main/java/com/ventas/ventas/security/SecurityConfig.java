package com.ventas.ventas.security;

import com.ventas.ventas.DTOs.Login.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/registro").permitAll()
                        .requestMatchers("/api/usuario/perfil-completo").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/usuario/avatar").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/productos/crear").hasRole("admin")
                        .requestMatchers(HttpMethod.POST, "/api/admin/crear-trabajador").hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/api/trabajador/pedidos-pendientes").hasAnyRole("trabajador", "admin")
                        .requestMatchers(HttpMethod.PUT, "/api/trabajador/pedidos/*/entregado").hasAnyRole("trabajador", "admin")
                        .requestMatchers("/api/menu/obtener").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/crear").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").authenticated()
                        .requestMatchers("/api/auth/test").permitAll()
                        .requestMatchers("/api/auth/test-db").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/trabajador/**").hasAnyRole("trabajador", "admin")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost",
                "http://localhost:80",
                "http://frontend",
                "http://frontend:80",
                "https://tacontento.up.railway.app",
                "http://tacontento.up.railway.app",
                
                "https://frontend-proyecto-venta-2.onrender.com",
                "http://frontend-proyecto-venta-2.onrender.com",
                "https://backend-proyecto-venta-2.onrender.com",
                "http://backend-proyecto-venta-2.onrender.com",

                "https://tacontento.onrender.com",
                "http://tacontento.onrender.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}