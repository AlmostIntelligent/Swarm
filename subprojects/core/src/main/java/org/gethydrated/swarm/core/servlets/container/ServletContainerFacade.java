package org.gethydrated.swarm.core.servlets.container;

import javax.servlet.Servlet;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class ServletContainerFacade {

	private String name;
	
	private ApplicationContext ctx;
	
	public ServletContainerFacade(String servletName, ActorContext rootContext,
			ActorRef rootRef, ApplicationContext ctx) {
		this.name = servletName;
		this.ctx = ctx;
	}

	public String getName() {
		return name;
	}

	public ActorRef ref() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setServletClass(String className) {
		// TODO Auto-generated method stub
		
	}

	public void setServletClass(Servlet servlet) {
		// TODO Auto-generated method stub
		
	}

	public ApplicationContext getServletContext() {
		return ctx;
	}

	public boolean setInitParameter(String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getInitParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServletName() {
		// TODO Auto-generated method stub
		return null;
	}

}
