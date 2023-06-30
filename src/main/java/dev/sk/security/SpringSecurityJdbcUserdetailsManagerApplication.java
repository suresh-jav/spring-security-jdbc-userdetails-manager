/**
 * This is just to learn the different flow of security. Don't implement this directly on PROD.
 * And for simplicity purpose, I have written everything in one file...
 */
package dev.sk.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@SpringBootApplication
public class SpringSecurityJdbcUserdetailsManagerApplication {

    @RestController
    static class MyController{
        @RequestMapping("/")
        public String main(){
            return "Hello Spring Security with JDBC Implementation...";
        }
    }
    @Configuration
    static  class ProjectConfig{

        @Bean
        public PasswordEncoder passwordEncoder(){
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
        @Bean
        public UserDetailsService userDetailsService(DataSource dataSource){
            return new JdbcUserDetailsManager(dataSource);
        }
    }
    @Configuration
//    @EnableWebSecurity(debug = true)
    @EnableWebSecurity
    static class SecurityConfig{
        @Autowired
        CustomAuth customAuth;
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)throws Exception{
            httpSecurity.authorizeRequests(x->x.anyRequest().authenticated());
            httpSecurity.formLogin(Customizer.withDefaults());
            return httpSecurity.build();
        }
        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity httpSecurity)throws Exception{
            AuthenticationManagerBuilder authenticationManagerBuilder =
                    httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
            authenticationManagerBuilder.authenticationProvider(customAuth);
            return authenticationManagerBuilder.build();
        }
    }

    @Configuration
    static class CustomAuth implements AuthenticationProvider{
        @Autowired
        UserDetailsService userDetailsService;
        @Autowired
        PasswordEncoder passwordEncoder;

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            UserDetails userDetails;
            try{
                userDetails = userDetailsService.loadUserByUsername(authentication.getName());
                if (!passwordEncoder.matches(authentication.getCredentials().toString(),userDetails.getPassword()))
                    throw new BadCredentialsException("");
            }catch (Exception exception){
                throw new BadCredentialsException("Invalid Username/Password");
            }
            return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(),userDetails.getAuthorities());
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }
    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityJdbcUserdetailsManagerApplication.class, args);
    }

}
