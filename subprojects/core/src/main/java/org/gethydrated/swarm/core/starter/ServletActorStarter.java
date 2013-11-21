package org.gethydrated.swarm.core.starter;

import org.gethydrated.swarm.core.servlets.WebAppRoot;
import org.gethydrated.swarm.core.servlets.WebAppScanner;

import akka.actor.Props;
import akka.actor.UntypedActor;

public class ServletActorStarter extends UntypedActor {

	@Override
	public void onReceive(Object o) throws Exception {
		unhandled(o);
	}
	
	@Override
	public void preStart() {
		context().system().actorOf(Props.create(WebAppRoot.class), "webapps");
		context().system().actorOf(Props.create(WebAppScanner.class), "scanner");
	}
	
}
