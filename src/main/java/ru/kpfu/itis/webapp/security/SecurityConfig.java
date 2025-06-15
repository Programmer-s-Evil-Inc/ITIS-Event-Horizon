package ru.kpfu.itis.webapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import ru.kpfu.itis.webapp.security.details.AccountUserDetailsService;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {
    private final DataSource dataSource;
    private final AccountUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(DataSource dataSource, AccountUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.dataSource = dataSource;
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(request -> {
//                    CorsConfiguration config = new CorsConfiguration();
//                    config.addAllowedOrigin("*");
//                    config.addAllowedMethod("*");
//                    config.addAllowedHeader("*");
//                    return config;
//                }))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/**").permitAll()  // 🟢 Разрешаем /api/auth/**
//                        // Организаторы
//                        .requestMatchers("/api/events/organizer/**").hasRole("ORGANIZER")
//                        .requestMatchers(HttpMethod.POST, "/api/events").hasRole("ORGANIZER")
//                        .requestMatchers(HttpMethod.POST, "/api/upload/event").hasRole("ORGANIZER")
//                        .requestMatchers(HttpMethod.GET, "/api/events/subscriptions/validate").hasRole("ORGANIZER")
//                        // Студенты и организаторы
//                        .requestMatchers(HttpMethod.POST, "/api/events/*/subscribe").hasAnyRole("STUDENT", "ORGANIZER")
//                        .requestMatchers(HttpMethod.GET, "/api/events/subscriptions").hasAnyRole("STUDENT", "ORGANIZER")
//                        .requestMatchers(HttpMethod.GET, "/api/events/*/qrcode").hasAnyRole("STUDENT", "ORGANIZER")
//                        // Профиль
//                        .requestMatchers("/api/profile/**").authenticated()
//                        .anyRequest().permitAll()
//                )
//                .rememberMe(remember -> remember
//                        .rememberMeParameter("rememberMe")
//                        .tokenRepository(tokenRepository())
//                        .tokenValiditySeconds(60 * 60 * 24 * 30)
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout")
//                )
//                .userDetailsService(userDetailsService)
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .formLogin(AbstractHttpConfigurer::disable);  // ❌ Выключаем formLogin
//
//        return http.build();
//    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Организаторы
                        .requestMatchers("/api/events/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/events").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/upload/event").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/events/subscriptions/validate").hasRole("ORGANIZER")

                        // Студенты и организаторы
                        .requestMatchers(HttpMethod.POST, "/api/events/*/subscribe").hasAnyRole("STUDENT", "ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/events/subscriptions").hasAnyRole("STUDENT", "ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/events/*/qrcode").hasAnyRole("STUDENT", "ORGANIZER")

                        // Общие правила
                        .requestMatchers("/api/profile/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/profile", true) // true - всегда перенаправлять
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeParameter("rememberMe")
                        .tokenRepository(tokenRepository())
                        .tokenValiditySeconds(60 * 60 * 24 * 30)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                )
                .userDetailsService(userDetailsService)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                            CorsConfiguration config = new CorsConfiguration();
                            config.addAllowedOrigin("*");
                            config.addAllowedMethod("*");
                            config.addAllowedHeader("*");
                            return config;
                        }
                ))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
