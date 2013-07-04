package org.gethydrated.swarm.server;

import java.util.Enumeration;

/**
 *
 */
public interface HttpMessage {

    String getHttpVersion();

    long getRequestId();

    String getHeader(String name);

    Enumeration<String> getHeaders(String name);

    Enumeration<String> getHeaderNames();
}
