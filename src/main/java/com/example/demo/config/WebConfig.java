package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@EnableWebSecurity
@Configuration
public class WebConfig {

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {    // Form에서 PUT, DELETE 메소드 사용을 위한 필터
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {  // Thymeleaf에서 Spring Security 사용을 위한 Bean 등록
        return new SpringSecurityDialect();
    }
}
