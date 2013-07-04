package org.gethydrated.swarm.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 *
 */
public class SwarmHttpResponse extends BaseHttpMessage implements HttpResponse, Serializable {

    private final transient Logger logger = LoggerFactory.getLogger(SwarmHttpResponse.class);
    private boolean keepAlive = false;
    private StringBuffer content = new StringBuffer();
    private String contentType = "";
    private int contentLength;
    private int status;

    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    public SwarmHttpResponse setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    @Override
    public StringBuffer getContentBuffer() {
        return content;
    }

    public SwarmHttpResponse setContent(StringBuffer content) {
        this.content = content;
        return this;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public SwarmHttpResponse setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    public SwarmHttpResponse setContentLength(int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
