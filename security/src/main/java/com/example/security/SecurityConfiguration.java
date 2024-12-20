package com.example.security;

import com.example.security.Filters.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    private UserDetailsServiceImpl  userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;


    public SecurityConfiguration(UserDetailsServiceImpl userDetailService) {
        this.userDetailsService = userDetailService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
     http.csrf().disable()
             .authorizeRequests().antMatchers("/login").permitAll()
             .and()
             .authorizeRequests().antMatchers("/register").permitAll()
             .anyRequest().authenticated()
             .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
     http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

                auth.userDetailsService(userDetailsService);

    }
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


}
