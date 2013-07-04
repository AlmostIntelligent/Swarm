package org.gethydrated.swarm.server;

/**
 *
 */
public interface HttpResponse extends HttpMessage {
    boolean isKeepAlive();

    StringBuffer getContentBuffer();

    String getContentType();

    int getContentLength();

    int getStatus();
}
