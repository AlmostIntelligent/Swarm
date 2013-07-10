package org.gethydrated.swarm.sessions;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SessionService {

    private final Map<String, SessionObject> sessions = new HashMap<>();

    public SessionObject get(String sessionId) {
        return sessions.get(sessionId);
    }

    public void put(SessionObject sobject) {
        sessions.put(sobject.getId(), sobject);
    }
}
