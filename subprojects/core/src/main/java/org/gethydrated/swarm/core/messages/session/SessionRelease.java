package org.gethydrated.swarm.core.messages.session;

import java.io.Serializable;

public class SessionRelease implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6729661840475737587L;
	private SessionObject session;
	
	public SessionRelease(SessionObject session) {
		this.session = session;
	}
	
	public SessionObject getSession() {
		return session;
	}
}
