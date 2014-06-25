package com._37coins.helper;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.restnucleus.WrappedRequest;

@Singleton
public class WrapFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
            WrappedRequest wrappedRequest = new WrappedRequest(
                    (HttpServletRequest) request);
            chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
