package org.gethydrated.swarm.core.servlets.container;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gethydrated.swarm.core.messages.container.Invoke.InvokeFilter;
import org.gethydrated.swarm.core.messages.container.Invoke.InvokeServlet;

import akka.actor.ActorRef;

public class ApplicationFilterChain implements FilterChain, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 178937646373482651L;
	private final ActorRef servlet;
	private final ActorRef source;
    private final LinkedList<ActorRef> filters;

    public ApplicationFilterChain(ActorRef servlet, ActorRef source) {
        this.servlet = servlet;
        this.source = source;
        this.filters = new LinkedList<>();
    }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		ActorRef fc = filters.poll();
		if (fc != null) {
			fc.tell(new InvokeFilter((HttpServletRequest)request, (HttpServletResponse)response, this), source);
		} else {
			servlet.tell(new InvokeServlet((HttpServletRequest)request, (HttpServletResponse)response), source);
		}
	}

	public void addFilters(List<FilterContainerFacade> filterContainers) {
		for (FilterContainerFacade f: filterContainers) {
			filters.add(f.ref());
		}
    }
}
