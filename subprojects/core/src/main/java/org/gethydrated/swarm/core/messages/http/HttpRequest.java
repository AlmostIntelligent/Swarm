package org.gethydrated.swarm.core.messages.http;

public interface HttpRequest extends HttpMessage {
    String getUri();

    String getMethod();

}