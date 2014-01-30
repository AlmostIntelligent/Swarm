package org.gethydrated.swarm.core.servlets.container;

public interface LifecycleAware {
	
	public void init() throws Exception;
	
	public void destroy() throws Exception;
	
	public LifecycleState getState();
	
	public String getStateName();
	
}
