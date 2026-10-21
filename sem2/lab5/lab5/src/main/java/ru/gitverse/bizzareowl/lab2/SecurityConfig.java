package ru.gitverse.bizzareowl.lab2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        UserDetails moderator = User.withDefaultPasswordEncoder()
                .username("moderator")
                .password("password")
                .roles("MODERATOR")
                .build();
        UserDetails manager = User.withDefaultPasswordEncoder()
                .username("manager")
                .password("password")
                .roles("MANAGER")
                .build();
        return new InMemoryUserDetailsManager(user, moderator, manager);
    }
}
