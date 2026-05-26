package com.bijlipay.config;

import com.bijlipay.security.CustomUserDetailsService;
import com.bijlipay.security.JwtAuthenticationEntryPoint;
import com.bijlipay.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.Filter;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Configuration
    @Order(1)
    public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Value("${basic.auth.user}")
        private String basicAuthUsername;
        @Value("${basic.auth.password}")
        private String basicAuthPassword;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/txn/tpt/**").authorizeRequests().anyRequest().fullyAuthenticated().and().httpBasic().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf().disable();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            auth.inMemoryAuthentication().withUser(basicAuthUsername).password(encoder.encode(basicAuthPassword)).roles("BASIC_USER");
        }
    }

    @Configuration
    @Order(2)
    public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        CustomUserDetailsService customUserDetailsService;
        @Autowired
        private JwtAuthenticationEntryPoint unauthorizedHandler;

        @Bean(BeanIds.AUTHENTICATION_MANAGER)
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter();
        }

        @Override
        public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
            authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.cors().and().csrf().disable().authorizeRequests().antMatchers("/auth/**", "/admin/**", "/txn/file/**",
                            "/otp/**", "/swagger/**", "/swagger-ui/**", "/swagger-ui.html/**", "/configuration/**",
                            "/swagger-ui/index.html/**", "/v3/api-docs/**", "/webjars/**", "/axisQr-txnNotifications",
                            "/actuator/health", "/mqtt-txn-publish", "/mqtt-txn-subscribe", "/insert-axis-missing-data",
                            "/actuator/prometheus/**", "/error,", "/txn/sync/**").permitAll()
                    .anyRequest().authenticated().and().exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                    .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            http.addFilterBefore((Filter) jwtAuthenticationFilter(), (Class<? extends Filter>) UsernamePasswordAuthenticationFilter.class);
        }

//        @Bean
//        CorsConfigurationSource corsConfigurationSource() {
//            CorsConfiguration configuration = new CorsConfiguration();
//            configuration.setAllowCredentials(true);
//            configuration.addAllowedOrigin("*");
//            configuration.addAllowedHeader("*");
//            configuration.addAllowedMethod("*");
//            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//            source.registerCorsConfiguration("/**", configuration);
//            return source;
//        }

        @Bean
        public CommonsRequestLoggingFilter requestLoggingFilter() {
            CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
            loggingFilter.setIncludeClientInfo(true);
            loggingFilter.setIncludeQueryString(false);
            loggingFilter.setIncludePayload(true);
            loggingFilter.setIncludeHeaders(true);
            return loggingFilter;
        }

        //        @Bean(name = "asyncExecutor")
//        public Executor asyncExecutor() {
//            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//            executor.setCorePoolSize(50);
//            executor.setMaxPoolSize(50);
//            executor.setQueueCapacity(100);
//            executor.setThreadNamePrefix("AsynchThread-");
//            executor.initialize();
//            return executor;
//        }
        @Bean(name = "asyncExecutor")
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
