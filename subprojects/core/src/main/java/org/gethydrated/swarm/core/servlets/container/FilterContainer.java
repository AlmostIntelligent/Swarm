package org.gethydrated.swarm.core.servlets.container;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FilterContainer implements LifecycleAware, FilterConfig {

	private final String name;
	private final ApplicationContext ctx;
	private String filterClass;
	private Filter filterInstance;
	private LifecycleState state = LifecycleState.CREATED;
	
	public FilterContainer(String name, ApplicationContext ctx) {
		this.name = name;
		this.ctx = ctx;
	}
	
	public void invoke(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (state == LifecycleState.RUNNING) {
			filterInstance.doFilter(request, response, filterChain);
		} else {
			ctx.getLogger().warning("Filter invoke while not in 'RUNNING' state. State was '{}'", state);
		}
	}
	
	@Override
	public String getFilterName() {
		return name;
	}

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public String getInitParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		if (state == LifecycleState.CREATED) {
			state = LifecycleState.INIT;
			try {
				state = LifecycleState.RUNNING;
				Class<?> clazz = ctx.getClassLoader().loadClass(filterClass);
				filterInstance = (Filter) clazz.newInstance();
				filterInstance.init(this);
			} catch (Throwable t) {
				ctx.getLogger().error(t, "Failed to initialize filter '{}'", name);
				state = LifecycleState.FAILED;
			}
		}
	}

	@Override
	public void destroy() {
		if (state == LifecycleState.RUNNING || state == LifecycleState.FAILED) {
			state = LifecycleState.DESTROY;
			try {
				if (filterInstance != null) {
					filterInstance.destroy();
					filterInstance = null;
				}
			} catch (Throwable t) {
				ctx.getLogger().error(t, "Failed to destroy filter '{}'", name);
			} finally {
				state = LifecycleState.STOPPED;
			}
		}
	}

	@Override
	public LifecycleState getState() {
		return state;
	}

	@Override
	public String getStateName() {
		return state.toString();
	}

	public boolean isComplete() {
		return filterClass != null;
	}

	public void setFilterClass(Filter filter) {
		if (filterClass == null && filter != null) {
            this.filterClass = filter.getClass().getName();
            this.filterInstance = filter;
        }
	}

	public void setFilterClass(String filterClass) {
		if (this.filterClass == null) {
            this.filterClass = filterClass;
        }
	}

	public String getFilterClass() {
		return filterClass;
	}

	

}
