package com.faultlab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地前后端分离开发需要开放跨域，生产环境可改为网关或 Nginx 代理。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${fault-lab.frontend-origin}")
    private String frontendOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(frontendOrigin)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(false)
            .maxAge(3600);
    }
}
