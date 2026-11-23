package com.bijlipay.security;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    private static final Gson gson = new Gson();

    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        response.setStatus(401);
        PrintWriter writer = response.getWriter();
        writer.println(gson.toJson("HTTP Status 401 : " + authException.getMessage()));
    }

    public void afterPropertiesSet() {
        this.setRealmName("Realm");
        super.afterPropertiesSet();
    }
}