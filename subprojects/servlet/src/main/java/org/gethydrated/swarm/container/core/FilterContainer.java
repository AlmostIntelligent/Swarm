package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.container.Container;
import org.slf4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    public void doInit() {

    }

    @Override
    public void doStart() {

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
        if (filterClass == null) {
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
                ", filterInstance=" + filterInstance +
                '}';
    }
}
