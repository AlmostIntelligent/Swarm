package org.gethydrated.swarm.core.servlets.container;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FilterFacade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7624168090912384187L;
	private final String name;

	public FilterFacade(FilterContainer filter) {
		this.name = filter.getFilterName();
	}

	public void invoke(ServletRequest request, ServletResponse response,
			ApplicationContext ctx, FilterChain chain) throws IOException, ServletException {
		ctx.log("filter " + name);
		ctx.getFilter(name).invoke(request, response, chain);
	}	
	
	@Override
	public String toString() {
		return "FilterFacade [name=" + name + "]";
	}
}
