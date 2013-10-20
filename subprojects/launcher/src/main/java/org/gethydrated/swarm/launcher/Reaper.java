package org.gethydrated.swarm.launcher;

import java.util.LinkedList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class Reaper extends UntypedActor {

	private List<ActorRef> souls = new LinkedList<>();
	
	private void allSoulsReaped() {
		this.context().system().shutdown();
		System.out.println("all reaped");
	}
	
	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof String && o.equals("WatchMe")) {
			System.out.println("new soul:" + sender());
			souls.add(sender());
			context().watch(sender());
		} else if (o instanceof Terminated) {
			souls.remove(((Terminated) o).actor());
			if (souls.isEmpty()) {
				allSoulsReaped();
			}
		}
		
	}

}
