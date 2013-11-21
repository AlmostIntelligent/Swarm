package org.gethydrated.swarm.core.servlets.container;

import javax.servlet.Filter;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class FilterContainerFacade {

	private ApplicationContext ctx;
	private String name;

	public FilterContainerFacade(String filterName, ActorContext rootContext,
			ActorRef rootRef, ApplicationContext ctx) {
		this.ctx = ctx;
		this.name = filterName;
	}

	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFilterClass(String className) {
		// TODO Auto-generated method stub
		
	}

	public void setFilterClass(Filter filter) {
		// TODO Auto-generated method stub
		
	}

	public ApplicationContext getServletContext() {
		return ctx;
	}

	public String getFilterName() {
		return name;
	}

	public String getFilterClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public ActorRef ref() {
		// TODO Auto-generated method stub
		return null;
	}

}
