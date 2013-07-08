package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.container.Container;
import org.gethydrated.swarm.container.LifecycleState;
import org.slf4j.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ServletContainer extends AbstractContainer implements ServletConfig {

    private String servletClass;
    private Servlet servletInstance;
    private Map<String, String> params = new HashMap<>();

    public ServletContainer(String name) {
        super(name);
    }

    public void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (servletInstance != null && getState() == LifecycleState.RUNNING) {
            servletInstance.service(request, response);
        }
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public void doInit() {
        if (servletInstance == null) {
            try {
                Class<?> clazz = getServletContext().getClassLoader().loadClass(servletClass);
                servletInstance = (Servlet) clazz.newInstance();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void doStart() {

    }

    @Override
    public void doStop() {

    }

    @Override
    public void doDestroy() {
        servletInstance = null;
    }

    @Override
    public void setParent(Container parent) {
        if (!(parent instanceof ApplicationContext)) {
            throw new IllegalArgumentException("Parent container must be an ApplicationContext.");
        }
        super.setParent(parent);
    }

    @Override
    public String getServletName() {
        return getName();
    }

    @Override
    public ServletContext getServletContext() {
        return (ServletContext) getParent();
    }

    public boolean setInitParameter(String name, String value) {
        if (params.containsKey(name)) {
            return false;
        }
        params.put(name, value);
        return true;
    }

    @Override
    public String getInitParameter(String name) {
        return params.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(params.keySet());
    }

    public boolean isComplete() {
        return servletClass != null;
    }

    public void setServletClass(String servletClass) {
        if (this.servletClass == null) {
            this.servletClass = servletClass;
        }
    }

    public void setServletClass(Servlet servlet) {
        if (servletClass == null && servlet != null) {
            this.servletClass = servlet.getClass().getName();
            this.servletInstance = servlet;
        }
    }

    public String getClassName() {
        return servletClass;
    }

    @Override
    public String toString() {
        return "ServletContainer{" +
                "servletClass='" + servletClass + '\'' +
                ", servletInstance=" + servletInstance +
                '}';
    }
}
