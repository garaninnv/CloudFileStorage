package com.garanin.CloudFileStorage.configurations;

//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;

//@EnableWebSecurity
//@RequiredArgsConstructor
//@Configuration
//@EnableMethodSecurity
public class SecurityConfig {

//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new MyUserDetailsService();
//    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(AbstractHttpConfigurer::disable)
////                .authorizeHttpRequests(auth -> auth.requestMatchers("welcome").permitAll()
////                        .requestMatchers("/**").authenticated())
//              //  .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
//     //           .formLogin(form -> form.loginPage("/login").permitAll())
//       //         .authorizeHttpRequests(auth-> auth.anyRequest().authenticated())
//                .formLogin(form -> form.loginPage("/login").permitAll())
//                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
//                .build();
//    }

//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService());
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
}
