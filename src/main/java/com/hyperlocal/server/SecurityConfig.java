package com.hyperlocal.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import javax.servlet.Filter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.StaticAllowFromStrategy;
import java.util.Arrays;
import java.net.URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception 
    {        
        httpSecurity
        .authorizeRequests()
        .antMatchers("/v1/merchants/{merchantID}/**")
        .access("@guard.hasValidIdToken(request,#merchantID)")
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .headers().frameOptions().disable()
        .addHeaderWriter(new XFrameOptionsHeaderWriter(new StaticAllowFromStrategy(new URI("microapps.google.com"))));
    }        
}