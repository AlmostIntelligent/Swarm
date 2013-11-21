package org.gethydrated.swarm.core.servlets.container;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

public interface Container extends Lifecycle {
	public LoggingAdapter getLogger();
	
	public String getName();
	
	public ActorRef getParent();
	
	public void setParent(ActorRef container);
}
