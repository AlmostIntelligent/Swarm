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
    private StringBuffer content;
    private String contentType = "";
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
        if (content == null) {
            content = new StringBuffer();
        }
        return content;
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
        return (content != null) ? content.length() : -1;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
