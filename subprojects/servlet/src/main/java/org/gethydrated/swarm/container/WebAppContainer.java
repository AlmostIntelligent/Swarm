package org.gethydrated.swarm.container;

import org.gethydrated.swarm.server.SwarmHttpRequest;
import org.gethydrated.swarm.server.SwarmHttpResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 *
 */
public interface WebAppContainer extends Container {

    void invoke(SwarmHttpRequest msg, SwarmHttpResponse response) throws ServletException, IOException;
}
