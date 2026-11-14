package com.rick.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String caminhoAudios = Paths.get("audios_bob").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/audios/**")
                .addResourceLocations(caminhoAudios);
    }
}
