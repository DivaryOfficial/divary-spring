package com.divary.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeHttpRequests ->
                authorizeHttpRequests
                        .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/", "/auth/**").permitAll() // 소셜 로그인 엔드포인트는 인증 없이 접근 가능
                        .anyRequest().authenticated() // 그 외의 요청은 인증이 필요
        );


        //임시로 꺼둠
        http.csrf((auth) -> auth.disable());

        return http.build();
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){

        return new BCryptPasswordEncoder();
    }
}
