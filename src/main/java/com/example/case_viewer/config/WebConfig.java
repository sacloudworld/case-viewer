package com.example.case_viewer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves the agent console (static/agent/index.html) at /agent/. The customer inbox is at /. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/agent", "/agent/");
        registry.addViewController("/agent/").setViewName("forward:/agent/index.html");
    }
}
