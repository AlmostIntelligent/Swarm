package org.gethydrated.swarm.core.servlets.container;

import static akka.pattern.Patterns.ask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.gethydrated.swarm.core.mapping.MappingInfo;
import org.gethydrated.swarm.core.messages.container.Invoke;
import org.gethydrated.swarm.core.messages.container.Invoke.InvokationResult;
import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletRequestWrapper;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletResponseWrapper;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.util.Timeout;

public class ApplicationRequestDispatcher implements RequestDispatcher {
	
	private final ServletContainerFacade servlet;
	private final ApplicationContext ctx;
	private final MappingInfo mInfo;
	
	public ApplicationRequestDispatcher(ApplicationContext applicationContext,
			ServletContainerFacade servletContainerFacade, MappingInfo info) {
		this.ctx = applicationContext;
		this.servlet = servletContainerFacade;
		this.mInfo = info;
	}

	@Override
	public void forward(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		System.out.println("forward");
		System.out.println(servlet.getName());
		SwarmHttpRequest req = ((SwarmServletRequestWrapper) request).unwrap().copy(ctx);
		
		req.setDispatcherType(DispatcherType.FORWARD);
		
		if (!mInfo.isEmpty()) {
			req.setContextPath(mInfo.contextPath);
			req.setPathInfo(mInfo.pathInfo);
			req.setServletPath(mInfo.servletPath);
		}
		
		SwarmHttpResponse res = ((SwarmServletResponseWrapper) response).unwrap();
		Timeout t = new Timeout(Duration.create(15, TimeUnit.SECONDS));
		Future<?> f = ask(servlet.ref(), new Invoke.InvokeServlet(req, res), t);
		try {
			Invoke.InvokationResult r = (InvokationResult) Await.result(f, t.duration());
			System.out.println(r);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void include(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		System.out.println("include");
		System.out.println(servlet.getName());
	}

}
