package org.gethydrated.swarm.container.core;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class ApplicationFilterChain implements FilterChain{

    private final ServletContainer servlet;

    public ApplicationFilterChain(ServletContainer servlet) {
        this.servlet = servlet;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        servlet.invoke((HttpServletRequest)request, (HttpServletResponse) response);
    }
}
