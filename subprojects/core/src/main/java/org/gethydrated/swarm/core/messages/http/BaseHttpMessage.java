package org.gethydrated.swarm.core.messages.http;

import javax.servlet.http.Cookie;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;

public class BaseHttpMessage implements HttpMessage, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3637719279223611627L;
	private String httpVersion;
    private long requestId;
    private Set<Pair> headers = new HashSet<>();
    private String serverName;
    private String localAddr;
    private String localHost;
    private int localPort;
    private String remoteAddr;
    private String remoteHost;
    private int remotePort;
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
        return localPort;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public String getLocalHost() {
        return localHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public BaseHttpMessage setRemoteInetAddr(InetSocketAddress address) {
        this.remoteAddr = address.getAddress().getHostAddress();
        this.remoteHost = address.getHostName();
        this.remotePort = address.getPort();
        return this;
    }

    public BaseHttpMessage setLocalInetAddr(InetSocketAddress address) {
        this.localAddr = address.getAddress().getHostAddress();
        this.localHost = address.getHostName();
        this.localPort = address.getPort();
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

    @Override
	public String toString() {
		return "BaseHttpMessage{ httpVersion=" + httpVersion + ", requestId="
				+ requestId + ", headers=" + headers + ", serverName="
				+ serverName + ", localAddr=" + localAddr + ", localHost="
				+ localHost + ", localPort=" + localPort + ", remoteAddr="
				+ remoteAddr + ", remoteHost=" + remoteHost + ", remotePort="
				+ remotePort + ", cookies=" + cookies + "}";
	}

	private static class Pair implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = -8075205219255030456L;
		private final String key;
        private final String value;

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    
    
}
