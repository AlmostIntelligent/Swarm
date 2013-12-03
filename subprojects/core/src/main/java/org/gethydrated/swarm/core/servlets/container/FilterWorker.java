package org.gethydrated.swarm.core.servlets.container;

import org.gethydrated.swarm.core.messages.container.ApplicationFilterChain;

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
			chain.doFilter();
			chain.source().tell(chain.response(), sender());
		} else {
			unhandled(o);
		}
	}
}
