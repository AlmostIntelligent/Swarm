package org.gethydrated.swarm.core.messages.container;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gethydrated.swarm.core.messages.container.Invoke.InvokationResult;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletRequestWrapper;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletResponseWrapper;
import org.gethydrated.swarm.core.servlets.container.ApplicationContext;
import org.gethydrated.swarm.core.servlets.container.FilterFacade;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.util.Timeout;
import static akka.pattern.Patterns.ask;

public class ApplicationFilterChain implements FilterChain, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 178937646373482651L;
	private final ActorRef servlet;
	private final ActorRef source;
	private SwarmServletRequestWrapper request;
	private SwarmServletResponseWrapper response;
    private final LinkedList<FilterFacade> filters;
	private transient ApplicationContext ctx;

    public ApplicationFilterChain(SwarmServletRequestWrapper request, SwarmServletResponseWrapper response, ActorRef servlet, ActorRef source) {
        this.servlet = servlet;
        this.source = source;
        this.request = request;
        this.response = response;
        this.filters = new LinkedList<>();
    }

    public void doFilter() throws IOException, ServletException {
    	doFilter(request, response);
    }
    
	@Override
	public void doFilter(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		FilterFacade fc = filters.poll();
		if (fc != null) {
			fc.invoke(request, response, ctx, this);
		} else {
			if (servlet != null) {
				Timeout t = new Timeout(Duration.create(5, TimeUnit.SECONDS));
				Future<?> f = ask(servlet, new Invoke.InvokeServlet((HttpServletRequest)request, (HttpServletResponse)response), t);
				try {
					Invoke.InvokationResult res = (InvokationResult) Await.result(f, t.duration());
					response = res.response;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				ctx.getLogger().warning("servlet is null for {}", ((SwarmServletRequestWrapper)request).getRequestURL());
			}
		}
	}

	public void addFilters(List<FilterFacade> filterContainers) {
		filters.addAll(filterContainers);
    }

	@Override
	public String toString() {
		return "ApplicationFilterChain [servlet=" + servlet + ", source="
				+ source + ", filters=" + filters + "]";
	}

	public void setContext(ApplicationContext ctx) {
		this.ctx = ctx;
	}

	public SwarmHttpResponse response() {
		return response.unwrap();
	}

	public ActorRef source() {
		return source;
	}
}
