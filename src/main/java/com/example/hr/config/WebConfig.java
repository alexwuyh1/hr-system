package com.example.hr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/dist/**")
        .addResourceLocations("file:src/main/resources/static/dist/", "classpath:/static/dist/")
        .setCachePeriod(0)
        .resourceChain(true);

    registry.addResourceHandler("/**")
        .addResourceLocations("file:src/main/resources/static/", "classpath:/static/")
        .setCachePeriod(0)
        .resourceChain(true);
  }
}
