package org.gethydrated.swarm.core.messages.http;

public interface HttpResponse extends HttpMessage {
    boolean isKeepAlive();

    String getContent();

    String getContentType();

    int getContentLength();

    int getStatus();
}