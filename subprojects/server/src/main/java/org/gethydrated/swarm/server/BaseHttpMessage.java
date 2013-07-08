package org.gethydrated.swarm.server;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 */
public class BaseHttpMessage implements HttpMessage {
    private String httpVersion;
    private long requestId;
    private Set<Pair> headers = new HashSet<>();
    private String host;
    private int localPort;

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

    public BaseHttpMessage setHost(String host) {
        this.host = host;
        return this;
    }

    public String getHost() {
        return host;
    }

    public BaseHttpMessage setLocalPort(int localPort) {
        this.localPort = localPort;
        return this;
    }

    public int getLocalPort() {
        return localPort;
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
