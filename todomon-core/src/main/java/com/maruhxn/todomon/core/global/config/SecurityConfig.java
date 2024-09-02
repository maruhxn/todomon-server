package com.maruhxn.todomon.core.global.config;

import com.maruhxn.todomon.core.global.auth.application.TodomonOAuth2UserService;
import com.maruhxn.todomon.core.global.auth.filter.JwtExceptionFilter;
import com.maruhxn.todomon.core.global.auth.filter.JwtVerificationFilter;
import com.maruhxn.todomon.core.global.auth.handler.JwtAccessDeniedHandler;
import com.maruhxn.todomon.core.global.auth.handler.JwtLogoutSuccessHandler;
import com.maruhxn.todomon.core.global.auth.handler.OAuth2EntryPoint;
import com.maruhxn.todomon.core.global.auth.handler.OAuth2LoginSuccessHandler;
import com.maruhxn.todomon.core.global.common.PermitAllUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TodomonOAuth2UserService todomonOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2EntryPoint oAuth2EntryPoint;
    private final JwtVerificationFilter jwtVerificationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring()
                        .requestMatchers("/static/favicon.ico")
                        .requestMatchers(PathRequest.toH2Console())
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(CsrfConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers("/").permitAll();
                    authz.requestMatchers(PathRequest.toH2Console()).permitAll();
                    Arrays.stream(PermitAllUrls.values()).forEach(url -> {
                        authz.requestMatchers(url.getMethod(), url.getUrl()).permitAll();
                    });
                    authz.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 ->
                        oauth2
                                .userInfoEndpoint(userInfoEndpointConfig ->
                                        userInfoEndpointConfig
                                                .userService(todomonOAuth2UserService)
                                )
                                .successHandler(oAuth2LoginSuccessHandler)
                )
                .logout(logout ->
                        logout
                                .clearAuthentication(true)
                                .invalidateHttpSession(true)
                                .logoutUrl("/api/auth/logout")
                                .logoutSuccessHandler(jwtLogoutSuccessHandler)
                )
                .addFilterBefore(jwtVerificationFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtVerificationFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(oAuth2EntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler));

        return http.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
