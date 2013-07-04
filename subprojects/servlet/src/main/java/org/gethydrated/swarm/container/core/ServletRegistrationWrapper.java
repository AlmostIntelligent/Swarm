package org.gethydrated.swarm.container.core;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.ServletSecurityElement;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ServletRegistrationWrapper implements Dynamic {

    private final ServletContainer container;

    public ServletRegistrationWrapper(ServletContainer container) {
        this.container = container;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {

    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {

    }

    @Override
    public void setRunAsRole(String roleName) {

    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {

    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        return ((ApplicationContext)container.getServletContext()).addServletMapping(container, urlPatterns);
    }

    @Override
    public Collection<String> getMappings() {
        return ((ApplicationContext)container.getServletContext()).getServletMapping(container);
    }

    @Override
    public String getRunAsRole() {
        return null;
    }

    @Override
    public String getName() {
        return container.getServletName();
    }

    @Override
    public String getClassName() {
        return container.getClassName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return container.setInitParameter(name, value);
    }

    @Override
    public String getInitParameter(String name) {
        return container.getInitParameter(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return null;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }
}
