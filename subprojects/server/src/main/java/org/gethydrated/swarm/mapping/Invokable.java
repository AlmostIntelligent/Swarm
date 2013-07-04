package org.gethydrated.swarm.mapping;

import org.gethydrated.swarm.server.SwarmHttpRequest;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 *
 */
public interface Invokable {
    void invoke(SwarmHttpRequest request) throws ServletException, IOException;
    void stop() throws Exception;
}
