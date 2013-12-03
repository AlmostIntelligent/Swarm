package org.gethydrated.swarm.core.servlets.container;

import javax.servlet.ServletRegistration.Dynamic;

import akka.actor.ActorRef;

public class ServletContainerFacade {

	private ServletRegistrationWrapper registration;
	private ActorRef ref;
	
	public ServletContainerFacade(String servletName, ApplicationContext ctx) {
		this.registration = new ServletRegistrationWrapper(servletName, ctx);
	}

	public String getName() {
		return registration.getName();
	}

	public ActorRef ref() {
		return ref;
	}

	public Dynamic getRegistration() {
		return registration;
	}

	public void setRef(ActorRef ref) {
		this.ref = ref;
	}

}
