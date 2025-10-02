package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration  // Spring 설정 클래스임을 나타냄
@EnableWebSecurity  // Spring Security를 활성화
@EnableMethodSecurity // 메소드 보안 활성화 (예: @PreAuthorize, @PostAuthorize 등)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth // 요청에 대한 권한 설정
                .requestMatchers("/","/login", "/register").permitAll()  // 로그인 & 회원가입은 누구나 접근 가능
                .requestMatchers( "/postlist/**", "/download/**").hasAnyRole("USER", "ADMIN")  // ROLE_USER 또는 ROLE_ADMIN 권한이 필요한 요청
                .requestMatchers("/userlist/**").hasRole("ADMIN")  // ROLE_ADMIN 권한이 필요한 요청
                .anyRequest().authenticated()  // 그 외 요청은 인증 필요
            )
            .formLogin(login -> login   // 폼 로그인 설정
                .loginPage("/login")  // 커스텀 로그인 페이지 사용
                .defaultSuccessUrl("/", true)  // 로그인 성공 시 이동할 페이지
                .failureUrl("/login?error=true")  // 로그인 실패 시 이동할 페이지
            )
            .logout(logout -> logout    // 로그아웃 설정
                .logoutUrl("/logout")  // 로그아웃 URL 설정
                .logoutSuccessUrl("/")  // 로그아웃 성공 시 이동할 페이지
                .invalidateHttpSession(true)  // 세션 무효화
                .deleteCookies("JSESSIONID")  // 쿠키 삭제
            );
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호 암호화
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();  // 인증 매니저 설정
    }
}
