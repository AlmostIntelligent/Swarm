package org.gethydrated.swarm.core.messages.session;

import java.io.Serializable;

public class SessionRegister implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5669196990864380659L;
	private SessionObject session;
	
	public SessionRegister(SessionObject session) {
		this.session = session;
	}
	
	public SessionObject getSession() {
		return session;
	}
}
