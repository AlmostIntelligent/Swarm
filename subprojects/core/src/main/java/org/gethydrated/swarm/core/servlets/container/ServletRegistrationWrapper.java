package org.gethydrated.swarm.core.servlets.container;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.ServletSecurityElement;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 */
public class ServletRegistrationWrapper implements Dynamic, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4055863902113082131L;
	@SuppressWarnings("unused")
	private int loadOnStartup = 0;
	private String servletClass;
	private Servlet servletInstance;
	private final String name;
	private boolean initialized = false;
	private final ApplicationContext ctx;
	
	private Map<String, String> parameters = new HashMap<>();
	
    public ServletRegistrationWrapper(String name, ApplicationContext ctx) {
    	this.name = name;
    	this.ctx = ctx;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
    	this.loadOnStartup = loadOnStartup;
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        return new HashSet<String>();
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
    	if (urlPatterns == null) {
    		throw new IllegalArgumentException("urlPatterns was null.");
    	}
    	if (initialized) {
    		throw new IllegalStateException("Servlet already initialized.");
    	}
    	Set<String> conflicts = new HashSet<>();
    	for (String s : urlPatterns) {
			if (ctx.hasServletMapping(s)) {
				conflicts.add(s);
			}
    	}
    	if (conflicts.isEmpty()) {
    		for (String s : urlPatterns) {
    			ctx.addServletMapping(name, s);
    		}
    	}
    	return conflicts;
    }

    @Override
    public Collection<String> getMappings() {
    	return ctx.getServletMapping(name);
    }

    @Override
    public String getRunAsRole() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return servletClass;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (name == null || value == null) {
        	throw new IllegalArgumentException("Name and value must not be null.");
        }
    	if (initialized) {
    		throw new IllegalStateException("Servlet already initialized.");
    	}
    	if (parameters.containsKey(name)) {
    		return false;
    	}
    	parameters.put(name, value);
    	return true;
    }

    @Override
    public String getInitParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
    	Set<String> conflicts = new HashSet<>();
    	for (Entry<String, String> e : initParameters.entrySet()) {
    		if (e.getKey() == null || e.getValue() == null) {
    			throw new IllegalArgumentException("Name and value must not be null.");
    		}
    		if (getInitParameter(e.getKey()) != null) {
    			conflicts.add(e.getKey());
    		}
    	}
    	if (conflicts.isEmpty()) {
    		for (Entry<String, String> e : initParameters.entrySet()) {
    			setInitParameter(e.getKey(), e.getValue());
    		}
    	}
        return conflicts;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return new HashMap<>(parameters);
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
	
	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public Servlet getInstance() {
		return servletInstance;
	}
}
