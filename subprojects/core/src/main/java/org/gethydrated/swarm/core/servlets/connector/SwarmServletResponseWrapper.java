package org.gethydrated.swarm.core.servlets.connector;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;
import org.gethydrated.swarm.core.servlets.container.ApplicationContext;

public class SwarmServletResponseWrapper implements HttpServletResponse, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6247914187750701535L;

	private transient ApplicationContext context;

    private final SwarmHttpResponse response;

    private transient PrintWriter writer;

    public SwarmServletResponseWrapper(SwarmHttpResponse response) {
        this.response = response;
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
        cookie.setMaxAge(30000);
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override

    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return "";
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return "";
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        setStatus(sc);
    }

    @Override
    public void sendError(int sc) throws IOException {
        setStatus(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeaders(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {
        response.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        //deprecated, ignore
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new ServletWriter(response));
        }
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {
        response.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

	public ApplicationContext getContext() {
		return context;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}
	
	public SwarmHttpResponse unwrap() {
		return response;
	}

	@Override
	public String toString() {
		return "SwarmServletResponseWrapper [response=" + response + "]";
	}
}
