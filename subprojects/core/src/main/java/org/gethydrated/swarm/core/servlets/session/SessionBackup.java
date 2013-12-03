package org.gethydrated.swarm.core.servlets.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gethydrated.swarm.core.messages.container.Beacon.StartBeacon;
import org.gethydrated.swarm.core.messages.container.Beacon.WebAppBeacon;
import org.gethydrated.swarm.core.messages.session.SessionBeacon;
import org.gethydrated.swarm.core.messages.session.SessionNotFound;
import org.gethydrated.swarm.core.messages.session.SessionObject;
import org.gethydrated.swarm.core.messages.session.SessionRequest;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;

public class SessionBackup extends UntypedActor {

	private final Map<String, SessionObject> sessions = new HashMap<>();
	private ActorRef mediator;
	private Cancellable timertask;
	
	@Override
	public void onReceive(Object o) throws Exception {
		if(o instanceof StartBeacon) {
			mediator.tell(new DistributedPubSubMediator.Publish("session-backup", new SessionBeacon()), self());
		} else if (o instanceof SessionObject) {
			sessions.put(((SessionObject) o).getId(), (SessionObject) o);
		} else if (o instanceof SessionRequest) {
			SessionObject s = sessions.get(((SessionRequest) o).getId());
			if (s != null) {
				sender().tell(s, self());
			} else {
				sender().tell(new SessionNotFound(), self());
			}
		}
	}
	
	public void preStart() {
		mediator = DistributedPubSubExtension.get(context().system()).mediator();
		timertask = context().system().scheduler().schedule(new FiniteDuration(0, TimeUnit.SECONDS), new FiniteDuration(1, TimeUnit.SECONDS), 
				self(), new StartBeacon(), context().system().dispatcher(), self());
	}

	public void postStop() {
		timertask.cancel();
	}
}
