package com.room5.server.spring_boot.web.authentication;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http
	    .csrf().disable()
	    
	    //if you got here to the last security filter, force authorization
	    .authorizeRequests()
	    
	    //allow anyone access to start
	    .antMatchers("/**").permitAll()
	    
	    ;
    }
}