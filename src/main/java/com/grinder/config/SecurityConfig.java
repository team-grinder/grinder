package com.grinder.config;

import com.grinder.repository.MemberRepository;
import com.grinder.security.MemberDetailsService;
import com.grinder.security.filter.APILoginFilter;
import com.grinder.security.filter.RefreshTokenFilter;
import com.grinder.security.filter.TokenCheckFilter;
import com.grinder.security.handler.APILoginFailureHandler;
import com.grinder.security.handler.APILoginSuccessHandler;
import com.grinder.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Slf4j
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final MemberDetailsService memberDetailsService;
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    public SecurityConfig(MemberDetailsService memberDetailsService, MemberRepository memberRepository, JWTUtil jwtUtil) {
        this.memberDetailsService = memberDetailsService;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public WebSecurityCustomizer configure() {      // 1) 스프링 시큐리티 기능 비활성화
        return web -> web.ignoring().requestMatchers(toH2Console())
                .requestMatchers("/static/**");
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        //AuthenticationManager 설정
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        // 인증 유저 관련
        authenticationManagerBuilder
                .userDetailsService(memberDetailsService)
                .passwordEncoder(passwordEncoder());
        // AuthenticationManager 빌드
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        // 설정 저장 필수
        http.authenticationManager(authenticationManager);

        //APILOGINFilter
        APILoginFilter apiLoginFilter = new APILoginFilter("/api/login");
        apiLoginFilter.setAuthenticationManager(authenticationManager);

        //APILoginFilter의 위치 조정
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);
        // 인증 성공 후처리 담당
        APILoginSuccessHandler successHandler = new APILoginSuccessHandler(jwtUtil);
        apiLoginFilter.setAuthenticationSuccessHandler(successHandler);

        APILoginFailureHandler failureHandler = new APILoginFailureHandler();
        apiLoginFilter.setAuthenticationFailureHandler(failureHandler);

        http.addFilterBefore(
                tokenCheckFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
        );
        http.addFilterBefore(new RefreshTokenFilter("/refreshToken",jwtUtil),
                TokenCheckFilter.class);
        http.csrf(csrf->csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->              // 인증, 인가 설정
                        auth.requestMatchers("/").permitAll() //TODO: url 추가
//                                .anyRequest().authenticated())
//                                .requestMatchers("/api/admin/").hasRole("관리자")
                                .anyRequest().permitAll());
        return http.build();

    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    private TokenCheckFilter tokenCheckFilter(JWTUtil jwtUtil){
        return new TokenCheckFilter(jwtUtil);
    }
}