package org.gethydrated.swarm.core.servlets.container;

import org.gethydrated.swarm.core.messages.container.ApplicationFilterChain;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;

import akka.actor.UntypedActor;

public class FilterWorker extends UntypedActor {

	private final ApplicationContext ctx;
	
	public FilterWorker(ApplicationContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof ApplicationFilterChain) {
			ApplicationFilterChain chain = (ApplicationFilterChain) o;
			chain.setContext(ctx);
			try {
				chain.doFilter();
				chain.source().tell(chain.response(), sender());
			} catch (Exception e) {
				SwarmHttpResponse r = chain.response();
		        r.setStatus(504);
		        r.setContent("Gateway timed out.");
		        r.setContentType("text/plain");
		        sender().tell(r,sender());
			}
		} else {
			unhandled(o);
		}
	}
}
