package org.gethydrated.swarm.container.core;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class FilterRegistrationWrapper implements FilterRegistration.Dynamic {

    private final FilterContainer container;

    public FilterRegistrationWrapper(FilterContainer container) {
        this.container = container;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {

    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {

    }

    @Override
    public Collection<String> getServletNameMappings() {
        return null;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        ((ApplicationContext)this.container.getServletContext()).addFilterMappingUrl(dispatcherTypes, isMatchAfter, urlPatterns, container);
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return null;
    }

    @Override
    public String getName() {
        return container.getFilterName();
    }

    @Override
    public String getClassName() {
        return container.getFilterClass();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
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
