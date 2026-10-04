package com.emergent.pos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Equivalent of the FastAPI backend's CORSMiddleware(allow_origins=CORS_ORIGINS, ...),
// same CORS_ORIGINS env var (defaults to "*" = allow all origins).
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        var mapping = registry.addMapping("/**")
                .allowedMethods("*")
                .allowedHeaders("*");
        if (origins.length == 1 && origins[0].trim().equals("*")) {
            mapping.allowedOriginPatterns("*").allowCredentials(true);
        } else {
            mapping.allowedOrigins(origins).allowCredentials(true);
        }
    }
}
