package org.gethydrated.swarm.core.starter;

import org.gethydrated.swarm.core.servlets.WebAppRoot;
import org.gethydrated.swarm.core.servlets.WebAppScanner;
import org.gethydrated.swarm.core.servlets.session.SessionBackup;
import org.gethydrated.swarm.core.servlets.session.SessionManager;

import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.contrib.pattern.ClusterSingletonManager;
import akka.contrib.pattern.ClusterSingletonPropsFactory;

public class ServletActorStarter extends UntypedActor {

	@Override
	public void onReceive(Object o) throws Exception {
		unhandled(o);
	}
	
	@Override
	public void preStart() {
		context().system().actorOf(Props.create(WebAppRoot.class), "webapps");
		context().system().actorOf(Props.create(WebAppScanner.class), "scanner");
		context().system().actorOf(Props.create(SessionBackup.class), "sessions");
		context().system().actorOf(ClusterSingletonManager.defaultProps("session", PoisonPill.getInstance(), "servlet", new ClusterSingletonPropsFactory() {

			private static final long serialVersionUID = 4386565372660673731L;

			@Override
			public Props create(Object arg0) {
				return Props.create(SessionManager.class);
			}
			
		}), "singleton");
	}
	
}
