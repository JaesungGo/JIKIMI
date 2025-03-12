package org.scoula.config;

import lombok.extern.slf4j.Slf4j;
import org.scoula.oauth.jwt.service.JwtService;
import org.scoula.oauth.jwt.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        log.info("SecurityConfig: JwtAuthenticationFilter");
        return new JwtAuthenticationFilter(jwtService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 비활성화
                .authorizeRequests()
                .antMatchers("/").permitAll() // 로그인, 소셜 로그인 엔드포인트는 모두 접근 가능
                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info("http : " + http);
        log.info("HttpSecurity configuration complete");
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/resources/**"); // 리소스 파일은 인증 없이 접근 가능
    }
}
