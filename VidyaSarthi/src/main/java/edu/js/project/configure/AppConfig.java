package edu.js.project.configure;


import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;



import org.springframework.web.servlet.config.annotation.CorsRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration

public class AppConfig {


    // global CORS if you prefer not to use @CrossOrigin on controllers

    @Bean

    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override

            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**")

                        .allowedOrigins("http://localhost:5175")

                        .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")

                        .allowedHeaders("*");

            }

        };

    }

}