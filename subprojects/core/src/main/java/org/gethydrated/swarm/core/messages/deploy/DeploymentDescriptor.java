package org.gethydrated.swarm.core.messages.deploy;

import java.util.*;

/**
 *
 */
public class DeploymentDescriptor {

    private List<ServletDescriptor> servlets = new LinkedList<>();
    private List<FilterDescriptor> filters = new LinkedList<>();
    private Map<String, Mapping> servletMappings = new HashMap<>();
    private Map<String, Mapping> filterMappings = new HashMap<>();

    public void addServlet(ServletDescriptor servletDescriptor) {
        if (servletDescriptor != null) {
            servlets.add(servletDescriptor);
        }
    }

    public List<ServletDescriptor> getServlets() {
        return servlets;
    }

    public void addFilter(FilterDescriptor filterDescriptor) {
        if (filterDescriptor != null) {
            filters.add(filterDescriptor);
        }
    }

    public List<FilterDescriptor> getFilters() {
        return filters;
    }

    public void addServletMapping(Mapping mapping) {
        servletMappings.put(mapping.getName(), mapping);
    }

    public Set<String> getServletMappings() {
        return servletMappings.keySet();
    }

    public Mapping getServletMapping(String name) {
        return servletMappings.containsKey(name) ? servletMappings.get(name) : new Mapping();
    }

    public void addFilterMapping(Mapping mapping) {
        filterMappings.put(mapping.getName(), mapping);
    }

    public Set<String> getFilterMappings() {
        return filterMappings.keySet();
    }

    public Mapping getFilterMapping(String name) {
        return filterMappings.containsKey(name) ? filterMappings.get(name) : new Mapping();
    }

    @Override
    public String toString() {
        return "DeploymentDescriptor{" +
                "servlets=" + servlets +
                ", filters=" + filters +
                ", servletMappings=" + servletMappings +
                ", filterMappings=" + filterMappings +
                '}';
    }
}
