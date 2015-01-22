package com.room5.server.spring_boot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.room5.server.spring_boot.jmx.JMXServerStatistics;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages="com.room5.server.spring_boot.web")
public class WebApplicationConfig extends WebMvcConfigurerAdapter
{

    @Autowired
    JMXServerStatistics jmxServerStatistics;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JMXInterceptor(jmxServerStatistics)).addPathPatterns("/demo/**");;
    }

}
