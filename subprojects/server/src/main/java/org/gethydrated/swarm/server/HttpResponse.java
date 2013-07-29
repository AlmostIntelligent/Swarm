package org.gethydrated.swarm.server;

/**
 *
 */
public interface HttpResponse extends HttpMessage {
    boolean isKeepAlive();

    String getContent();

    String getContentType();

    int getContentLength();

    int getStatus();
}
