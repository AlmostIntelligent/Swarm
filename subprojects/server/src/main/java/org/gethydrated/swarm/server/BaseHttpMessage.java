package org.gethydrated.swarm.server;

import javax.servlet.http.Cookie;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 */
public class BaseHttpMessage implements HttpMessage {
    private String httpVersion;
    private long requestId;
    private Set<Pair> headers = new HashSet<>();
    private String serverName;
    private InetSocketAddress localAddr;
    private InetSocketAddress remoteAddr;
    private Set<Cookie> cookies = new HashSet<>();

    @Override
    public String getHttpVersion() {
        return httpVersion;
    }

    public BaseHttpMessage setHttpVersion(String version) {
        this.httpVersion = version;
        return this;
    }

    public BaseHttpMessage addHeaders(List<Entry<String, String>> headers) {
        for (Entry<String, String> e : headers) {
            this.headers.add(new Pair(e.getKey(), e.getValue()));
        }
        return this;
    }

    public void addHeaders(String name, String value) {
        this.headers.add(new Pair(name, value));
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public String getHeader(String name) {
        for (Pair p : headers) {
            if (p.key.equals(name)) {
                return p.value;
            }
        }
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Set<String> results = new HashSet<>();
        for (Pair p : headers) {
            if (p.key.equals(name)) {
                results.add(p.value);
            }
        }
        return Collections.enumeration(results);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> results = new HashSet<>();
        for (Pair p : headers) {
            results.add(p.key);
        }
        return Collections.enumeration(results);
    }

    public BaseHttpMessage setRequestId(long requestId) {
        this.requestId = requestId;
        return this;
    }

    public BaseHttpMessage setServerName(String host) {
        this.serverName = host;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public int getLocalPort() {
        return localAddr.getPort();
    }

    public String getLocalAddr() {
        return localAddr.getAddress().getHostAddress();
    }

    public String getLocalHost() {
        return localAddr.getHostName();
    }

    public int getRemotePort() {
        return remoteAddr.getPort();
    }

    public String getRemoteHost() {
        return remoteAddr.getHostName();
    }

    public String getRemoteAddr() {
        return remoteAddr.getAddress().getHostAddress();
    }

    public BaseHttpMessage setRemoteAddr(InetSocketAddress address) {
        this.remoteAddr = address;
        return this;
    }

    public BaseHttpMessage setLocalAddr(InetSocketAddress address) {
        this.localAddr = address;
        return this;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public Set<Cookie> getCookies() {
        return cookies;
    }

    private static class Pair {
        private final String key;
        private final String value;

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
