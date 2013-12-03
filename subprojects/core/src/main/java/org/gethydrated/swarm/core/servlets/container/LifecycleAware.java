package org.gethydrated.swarm.core.servlets.container;

public interface LifecycleAware {
	
	public void init();
	
	public void destroy();
	
	public LifecycleState getState();
	
	public String getStateName();
	
}
