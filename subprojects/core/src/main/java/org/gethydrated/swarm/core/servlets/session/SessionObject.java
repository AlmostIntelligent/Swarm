package org.gethydrated.swarm.core.servlets.session;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class SessionObject implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7905891566478334463L;
	private final long creationTime;
    private String id;
    private Map<String, Object> attributes = new HashMap<>();

    public SessionObject() {
        this.creationTime = System.currentTimeMillis();
        this.id = changeId();
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getId() {
        return id;
    }

    public String changeId() {
        id = UUID.randomUUID().toString();
        return id;
    }

    public long getLastAccessedTime() {
        return 0;
    }

    public void setMaxInactiveInterval(int interval) {

    }

    public int getMaxInactiveInterval() {
        return 0;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    public void invalidate() {

    }

    public boolean isNew() {
        return false;
    }
}