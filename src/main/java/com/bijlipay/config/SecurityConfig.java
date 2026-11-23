package com.bijlipay.config;

import com.bijlipay.security.CustomUserDetailsService;
import com.bijlipay.security.JwtAuthenticationEntryPoint;
import com.bijlipay.security.JwtAuthenticationFilter;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@EnableAsync
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
public class SecurityConfig {
    @Configuration
    @Order(1)
    public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Value("${basic.auth.user}")
        private String basicAuthUsername;
        @Value("${basic.auth.password}")
        private String basicAuthPassword;

        protected void configure(HttpSecurity http) throws Exception {
            ((HttpSecurity)((HttpSecurity)((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)http.antMatcher("/txn/tpt/**").authorizeRequests().anyRequest()).fullyAuthenticated().and()).httpBasic().and()).sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()).csrf().disable();
        }

        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            auth.inMemoryAuthentication().withUser(this.basicAuthUsername).password(encoder.encode(this.basicAuthPassword)).roles(new String[]{"BASIC_USER"});
        }
    }

    @Configuration
    @Order(2)
    public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        CustomUserDetailsService customUserDetailsService;
        @Autowired
        private JwtAuthenticationEntryPoint unauthorizedHandler;

        @Bean({"org.springframework.security.authenticationManager"})
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter();
        }

        public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
            authenticationManagerBuilder.userDetailsService(this.customUserDetailsService).passwordEncoder(this.passwordEncoder());
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        protected void configure(HttpSecurity http) throws Exception {
            ((HttpSecurity)((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)((HttpSecurity)http.cors().and()).csrf().disable()).authorizeRequests().antMatchers(new String[]{"/auth/**", "/admin/**", "/txn/file/**", "/otp/**", "/swagger/**", "/swagger-ui/**", "/swagger-ui.html/**", "/configuration/**", "/swagger-ui/index.html/**", "/v3/api-docs/**", "/webjars/**", "/axisQr-txnNotifications", "/actuator/health", "/mqtt-txn-publish", "/mqtt-txn-subscribe", "/insert-axis-missing-data", "/actuator/prometheus/**", "/error,", "/txn/sync/**"})).permitAll().anyRequest()).authenticated().and()).exceptionHandling().authenticationEntryPoint(this.unauthorizedHandler).and()).sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            http.addFilterBefore(this.jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        }

        @Bean
        public CommonsRequestLoggingFilter requestLoggingFilter() {
            CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
            loggingFilter.setIncludeClientInfo(true);
            loggingFilter.setIncludeQueryString(false);
            loggingFilter.setIncludePayload(true);
            loggingFilter.setIncludeHeaders(true);
            return loggingFilter;
        }

        @Bean(
                name = {"asyncExecutor"}
        )
        public Executor asyncExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(20);
            executor.setMaxPoolSize(50);
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("AsyncThreads--");
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            executor.initialize();
            return executor;
        }
    }
}