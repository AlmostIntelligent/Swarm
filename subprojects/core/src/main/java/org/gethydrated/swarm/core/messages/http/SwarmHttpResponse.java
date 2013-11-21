package org.gethydrated.swarm.core.messages.http;

import java.io.Serializable;


public class SwarmHttpResponse extends BaseHttpMessage implements HttpResponse, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1005762846894087932L;
	private boolean keepAlive = false;
    private String content;
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
    public String getContent() {
        if (content == null) {
            content = "";
        }
        return content;
    }

    public void setContent(String c) {
        content = c;
    }

    public void appendContent(String c) {
        content = content + c;
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
        return (content != null) ? content.length() : 0;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "SwarmHttpResponse{" +
                "keepAlive=" + keepAlive +
                ", content=" + content +
                ", contentType='" + contentType + '\'' +
                ", status=" + status +
                '}';
    }
}
