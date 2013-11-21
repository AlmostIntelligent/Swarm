package org.gethydrated.swarm.core.starter;

import org.gethydrated.swarm.core.server.HttpRequestRouter;
import org.gethydrated.swarm.core.server.HttpServer;

import akka.actor.Props;
import akka.actor.UntypedActor;

public class HttpActorStarter extends UntypedActor {

	@Override
	public void onReceive(Object o) throws Exception {
		unhandled(o);
	}
	
	@Override
	public void preStart() {
		context().system().actorOf(Props.create(HttpRequestRouter.class), "http-request-router");
		context().system().actorOf(Props.create(HttpServer.class), "http-server");
	}

}
