package com.example.contructioninventoryapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps http://localhost:8080/uploads/myimage.jpg
        // to the "uploads" folder in your root directory.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}