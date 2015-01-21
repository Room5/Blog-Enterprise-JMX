package com.room5.server.spring_boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class JMXServiceServer extends AbstractServer
{
    private static final Logger logger = LoggerFactory.getLogger(JMXServiceServer.class);

    public static void main(String[] args) {
	SpringApplication.run(JMXServiceServer.class, args);
	logger.info("Server started.");
    }

}
