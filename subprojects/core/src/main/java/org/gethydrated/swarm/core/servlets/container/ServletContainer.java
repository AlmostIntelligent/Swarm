package org.gethydrated.swarm.core.servlets.container;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.gethydrated.swarm.core.messages.container.Invoke;
import org.gethydrated.swarm.core.messages.container.Invoke.InvokeServlet;
import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletRequestWrapper;
import org.gethydrated.swarm.core.servlets.connector.SwarmServletResponseWrapper;

import akka.actor.ActorRef;

public class ServletContainer extends AbstractContainer implements ServletConfig {

	private final ApplicationContext ctx;
	private final ServletRegistrationWrapper registration;
	private Servlet servletInstance;
	
	public ServletContainer(ApplicationContext ctx, String servletName, ActorRef parent) {
		super(servletName, parent);
		this.ctx = ctx;
		this.registration = (ServletRegistrationWrapper) ctx.getServletFacade(servletName).getRegistration();
		this.servletInstance = registration.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.gethydrated.swarm.core.servlets.container.AbstractContainer#doInit()
	 */
	@Override
	protected void doInit() throws Exception {
		getLogger().info("Servlet {} created", getName());
		ClassLoader c1 = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(ctx.getClassLoader());
			if (servletInstance == null) {
				Class<?> clazz = ctx.getClassLoader().loadClass(registration.getClassName());
				servletInstance = (Servlet) clazz.newInstance();
				servletInstance.init(this);
			}
		} finally {
			Thread.currentThread().setContextClassLoader(c1);
		}
		registration.setInitialized(true);
	}

	@Override
	protected void doDestroy() throws Exception {
		servletInstance = null;
		getLogger().info("Servlet {} destroyed", getName());
	}

	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof Invoke.InvokeServlet) {
			getLogger().info("Request: {} - {}", o, self());
			Invoke.InvokeServlet invokation = (InvokeServlet) o;
			if (getState() == LifecycleState.RUNNING) {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
		        try {
		            Thread.currentThread().setContextClassLoader(getServletContext().getClassLoader());
		            if (servletInstance != null && getState() == LifecycleState.RUNNING) {
		            	SwarmServletRequestWrapper wrequest = (SwarmServletRequestWrapper) invokation.request();
		            	SwarmServletResponseWrapper wresponse = (SwarmServletResponseWrapper) invokation.response();
		            	wrequest.setContext(ctx);
		            	wresponse.setContext(ctx);
		            	getLogger().info("{}", wresponse);
		                servletInstance.service(wrequest, wresponse);
		                getLogger().info("{}", wresponse);
		                getLogger().info("{}", invokation.response());
		                wresponse.getWriter().flush();
		            }
		        } finally {
		            Thread.currentThread().setContextClassLoader(cl);
		            sender().tell(new Invoke.InvokationResult(invokation.request(), invokation.response()), self());
		        }
			} else {
				invokation.response().setContentType("text/plain");
				invokation.response().getWriter().println("ERROR: Servlet not running");
				sender().tell(new Invoke.InvokationResult(invokation.request(), invokation.response()), self());
			}
		}
	}

	@Override
	public String getServletName() {
		return registration.getName();
	}

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public String getInitParameter(String name) {
		return registration.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.enumeration(registration.getInitParameters().keySet());
	}

}
