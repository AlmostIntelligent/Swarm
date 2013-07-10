package org.gethydrated.swarm.server;

import javax.servlet.http.Cookie;
import java.util.Enumeration;
import java.util.Set;

/**
 *
 */
public interface HttpMessage {

    String getHttpVersion();

    long getRequestId();

    String getHeader(String name);

    Enumeration<String> getHeaders(String name);

    Enumeration<String> getHeaderNames();

    Set<Cookie> getCookies();

    void addCookie(Cookie cookie);
}
