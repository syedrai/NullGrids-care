package com.medicare.config;

import com.medicare.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired private CustomUserDetailsService userDetailsService;

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/","/login","/register","/register/**","/css/**","/js/**","/img/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/patient/**").hasRole("PATIENT")
                .anyRequest().authenticated()
            )
            .formLogin(f -> f
                .loginPage("/login").loginProcessingUrl("/login")
                .usernameParameter("email").passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true").permitAll()
            )
            .logout(l -> l
                .logoutUrl("/logout").logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll()
            )
            .sessionManagement(s -> s.maximumSessions(1).maxSessionsPreventsLogin(false))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .headers(h -> h
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; font-src 'self' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net data:; img-src 'self' data:; connect-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com;"))
                .frameOptions(f -> f.deny())
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );
        return http.build();
    }
}
