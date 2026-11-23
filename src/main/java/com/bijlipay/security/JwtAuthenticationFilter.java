package com.bijlipay.security;

import com.bijlipay.exception.UnAuthorizedException;
import com.bijlipay.model.UserAuthentication;
import com.bijlipay.service.UserAuthenticationService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserAuthenticationService userAuthenticateService;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = this.getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
                int userId = this.tokenProvider.getUserIdFromJWT(jwt);
                UserAuthentication usertoken = this.userAuthenticateService.getUser((long)userId);
                if (!jwt.equals(usertoken.getToken())) {
                    JwtAuthenticationEntryPoint jwtAuthPoint = new JwtAuthenticationEntryPoint();
                    AuthenticationException e = null;

                    try {
                        jwtAuthPoint.commence(request, response, e);
                        throw new UnAuthorizedException("can't login same user on same time!");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                UserDetails userDetails = this.customUserDetailsService.loadUserById((long)userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, SecurityContextHolder.getContext().getAuthentication(), userDetails.getAuthorities());
                authentication.setDetails((new WebAuthenticationDetailsSource()).buildDetails(request));
                System.out.println("Authenticated User " + authentication.getName());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : null;
    }
}