package org.gethydrated.swarm.core.servlets.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.gethydrated.swarm.core.messages.session.SessionBeacon;
import org.gethydrated.swarm.core.messages.session.SessionObject;
import org.gethydrated.swarm.core.messages.session.SessionRelease;
import org.gethydrated.swarm.core.messages.session.SessionRequest;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;
import static akka.pattern.Patterns.ask;

public class SessionManager extends UntypedActor {

	private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	private List<ActorRef> backups = new LinkedList<>();
	private Map<String, ActorRef> waitlist = new HashMap<>();
	private Map<String, Long> leases = new HashMap<>();
	private Map<String, SessionObject> sessions = new HashMap<>();
	
	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof SessionBeacon) {
			if (!backups.contains(sender())) {
				logger.info("New session Backup found {}", sender());
				backups.add(sender());
				context().watch(getSender());
			}
			
		} else if (o instanceof Terminated) {
			logger.info("New session Backup disconntect {}", sender());
			backups.remove(sender());
		} else if (o instanceof SessionRequest) {
			SessionRequest r = (SessionRequest) o;
			if (sessions.containsKey(r.getId())) {
				SessionObject s = sessions.remove(r.getId());
				leases.put(r.getId(), System.currentTimeMillis());
				sender().tell(s, getSelf());
			} else {
				if (leases.containsKey(r.getId())) {
					waitlist.put(r.getId(), sender());
				} else {
					for (ActorRef a : backups) {
						Timeout t = new Timeout(Duration.create(5, TimeUnit.SECONDS));
						Future<?> f = ask(a, o, t);
						Object result = Await.result(f, t.duration());
						if (result instanceof SessionObject) {
							leases.put(r.getId(), System.currentTimeMillis());
							sender().tell(result, getSelf());
						}
						break;
					}
				}
			}
		} else if (o instanceof SessionRelease) {
			for (ActorRef a : backups) {
				a.tell(((SessionRelease) o).getSession(), sender());

			}
			if (waitlist.containsKey(((SessionRelease) o).getSession().getId())) {
				ActorRef ref = waitlist.remove(((SessionRelease) o).getSession().getId());
				ref.tell(((SessionRelease) o).getSession(), self());
			} else {
				sessions.put(((SessionRelease) o).getSession().getId(), ((SessionRelease) o).getSession());
			}
		}
	}

	@Override
	public void preStart() {
		ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
		mediator.tell(new DistributedPubSubMediator.Subscribe("session-backup", getSelf()), getSelf());
	}
}
