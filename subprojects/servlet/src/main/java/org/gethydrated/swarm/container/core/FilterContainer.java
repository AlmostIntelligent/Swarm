package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.container.Container;
import org.gethydrated.swarm.container.LifecycleState;
import org.slf4j.Logger;

import javax.servlet.*;
import java.io.IOException;
import java.util.Enumeration;

/**
 *
 */
public class FilterContainer extends AbstractContainer implements FilterConfig {

    private String filterClass;
    private Filter filterInstance;

    public FilterContainer(String name) {
        super(name);
    }

    public void invoke(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (getState() == LifecycleState.RUNNING) {
            filterInstance.doFilter(request, response, filterChain);
        }
    }

    /* ------------------------ Container methods --------------------------*/

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public void doInit() {
        if (filterInstance == null) {
            try {
                Class<?> clazz = getServletContext().getClassLoader().loadClass(filterClass);
                filterInstance = (Filter) clazz.newInstance();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void doStart() throws ServletException {
        filterInstance.init(this);
    }

    @Override
    public void doStop() {

    }

    @Override
    public void doDestroy() {

    }

    @Override
    public void setParent(Container parent) {
        if (!(parent instanceof ApplicationContext)) {
            throw new IllegalArgumentException("Parent container must be an ApplicationContext.");
        }
        super.setParent(parent);
    }

    @Override
    public String getFilterName() {
        return getName();
    }

    @Override
    public ServletContext getServletContext() {
        return (ServletContext) getParent();
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }

    public boolean isComplete() {
        return filterClass != null;
    }

    public void setFilterClass(String filterClass) {
        if (this.filterClass == null) {
            this.filterClass = filterClass;
        }
    }

    public void setFilterClass(Filter filter) {
        if (filterClass == null && filter != null) {
            this.filterClass = filter.getClass().getName();
            this.filterInstance = filter;
        }
    }

    public String getFilterClass() {
        return filterClass;
    }

    @Override
    public String toString() {
        return "FilterContainer{" +
                "filterClass='" + filterClass + '\'' +
                '}';
    }

}
