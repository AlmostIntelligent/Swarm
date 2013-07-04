package org.gethydrated.swarm.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 *
 */
public class SwarmHttpRequest extends BaseHttpMessage implements HttpRequest, Serializable {

    private final transient Logger logger = LoggerFactory.getLogger(SwarmHttpRequest.class);
    private String uri;
    private String method;

    public SwarmHttpRequest setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public SwarmHttpRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    @Override
    public String getMethod() {
        return method;
    }
}
