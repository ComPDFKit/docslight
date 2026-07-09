package com.compdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class CorsFilter implements Filter {

    @Value("${cors.allow-headers}")
    private String headers;
    @Value("${cors.allow-methods}")
    private String methods;
    @Value("${cors.allow-expose}")
    private String expose;
    @Value("${cors.allow-credentials}")
    private String credentials;
    @Value("${cors.allow-origins}")
    private String origins;
    @Value("${cors.allow-max-age}")
    private String maxAge;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", origins);
        response.setHeader("Access-Control-Allow-Credentials", credentials);
        response.setHeader("Access-Control-Allow-Methods", methods);
        response.setHeader("Access-Control-Allow-Max-Age", maxAge);
        response.setHeader("Access-Control-Expose-Headers", expose);
        response.setHeader("Access-Control-Allow-Headers", headers);
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
