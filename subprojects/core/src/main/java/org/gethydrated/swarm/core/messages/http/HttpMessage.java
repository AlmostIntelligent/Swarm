package org.gethydrated.swarm.core.messages.http;

import java.util.Enumeration;
import java.util.Set;
import javax.servlet.http.Cookie;

public interface HttpMessage {
	
    String getHttpVersion();

    long getRequestId();

    String getHeader(String name);

    Enumeration<String> getHeaders(String name);

    Enumeration<String> getHeaderNames();

    Set<Cookie> getCookies();

    void addCookie(Cookie cookie);
}
