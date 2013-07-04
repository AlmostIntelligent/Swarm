package org.gethydrated.swarm.server;

/**
 *
 */
public interface HttpRequest extends HttpMessage {
    String getUri();

    String getMethod();

}
