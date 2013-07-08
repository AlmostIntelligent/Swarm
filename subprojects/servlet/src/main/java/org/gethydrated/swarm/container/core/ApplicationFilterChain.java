package org.gethydrated.swarm.container.core;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ApplicationFilterChain implements FilterChain{

    private final ServletContainer servlet;
    private final LinkedList<FilterContainer> filters;

    public ApplicationFilterChain(ServletContainer servlet) {
        this.servlet = servlet;
        this.filters = new LinkedList<>();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        FilterContainer fc = filters.poll();
        if (fc != null) {
            fc.invoke(request, response, this);
        } else {
            servlet.invoke((HttpServletRequest)request, (HttpServletResponse) response);
        }
    }

    public void addFilters(List<FilterContainer> filterContainers) {
        filters.addAll(filterContainers);
    }
}
