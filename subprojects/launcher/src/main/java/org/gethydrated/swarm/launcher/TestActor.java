package org.gethydrated.swarm.launcher;

import akka.actor.UntypedActor;

public class TestActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		context().stop(context().self());
	}
	
	@Override
	public void preStart() {
		System.out.println("register");
		context().actorSelection("/user/reaper").tell("WatchMe", self());
	}

}
