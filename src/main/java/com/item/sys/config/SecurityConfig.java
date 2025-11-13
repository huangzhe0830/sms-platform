package com.item.sys.config;

import com.alibaba.fastjson.JSON;
import com.item.sys.http.HttpResult;
import com.item.sys.http.HttpResultEnum;
import com.item.sys.security.JwtAuthenticationFilter;
import com.item.sys.security.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)//开启secuity的注解
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;


    /* 下面按需配置密码加密、UserDetailsService、放行路径等 */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用X-Content-Type-Options
        http.headers().contentTypeOptions().disable();
        // 禁用 csrf, 由于使用的是JWT，我们这里不需要csrf
        http.headers().frameOptions().disable().and().cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/**/login").permitAll()
                .antMatchers("/ws/**").permitAll()
                .anyRequest().authenticated();

        // 退出登录处理器
        http.logout().logoutUrl("/v1/logout").logoutSuccessHandler((request, response, authentication) -> {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            out.write(JSON.toJSONString(HttpResult.ok(HttpResultEnum.SUCCESS)));
            out.flush();
            out.close();
        });

        // 前后端分离采用JWT 不需要session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // token验证过滤器
        http.addFilterBefore(new JwtAuthenticationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 使用自定义身份验证组件
        auth.authenticationProvider(new JwtAuthenticationProvider(userDetailsService));
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
