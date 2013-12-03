package org.gethydrated.swarm.core.servlets.connector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.gethydrated.swarm.core.messages.session.SessionObject;
import org.gethydrated.swarm.core.servlets.container.ApplicationContext;

import java.util.Enumeration;

/**
 *
 */
public class HttpSessionWrapper implements HttpSession {

    private final SessionObject sobject;

    private final ApplicationContext context;

    public HttpSessionWrapper(SessionObject sobject, ApplicationContext context) {
        this.sobject = sobject;
        this.context = context;
    }

    @Override
    public long getCreationTime() {
        return sobject.getCreationTime();
    }

    @Override
    public String getId() {
        return sobject.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return sobject.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        sobject.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return sobject.getMaxInactiveInterval();
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return sobject.getAttribute(name);
    }

    @Override
    @Deprecated
    public Object getValue(String name) {
        return false;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return sobject.getAttributeNames();
    }

    @Override
    @Deprecated
    public String[] getValueNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String name, Object value) {
        sobject.setAttribute(name, value);
    }

    @Override
    @Deprecated
    public void putValue(String name, Object value) {
    }

    @Override
    public void removeAttribute(String name) {
        Object o = sobject.removeAttribute(name);
        if (o != null && o instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) o).valueUnbound(new HttpSessionBindingEvent(this, name, o));
        }
    }

    @Override
    @Deprecated
    public void removeValue(String name) {
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isNew() {
        return false;
    }
}