package org.gethydrated.swarm.core.messages.session;

import java.io.Serializable;

public class SessionRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8831593821058595279L;
	private final String id;
	
	public SessionRequest(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
