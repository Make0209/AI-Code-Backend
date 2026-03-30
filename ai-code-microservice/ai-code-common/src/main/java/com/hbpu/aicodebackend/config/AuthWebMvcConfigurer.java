package com.hbpu.aicodebackend.config;

import com.hbpu.aicodebackend.auth.AuthCheckInterceptor;
import com.hbpu.aicodebackend.auth.UserContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AuthWebMvcConfigurer implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    private final AuthCheckInterceptor authCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor).addPathPatterns("/**").order(0);
        registry.addInterceptor(authCheckInterceptor).addPathPatterns("/**").order(1);
    }
}
