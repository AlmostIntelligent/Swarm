package org.gethydrated.swarm.core.servlets.container;

import akka.actor.ActorRef;

public class FilterContainer extends AbstractContainer {

	public FilterContainer(String name, ActorRef parent) {
		super(name, parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doDestroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String getFilterName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFilterClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public ApplicationContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
