package org.gethydrated.swarm.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SwarmHttpRequest extends BaseHttpMessage implements HttpRequest, Serializable {

    private final transient Logger logger = LoggerFactory.getLogger(SwarmHttpRequest.class);
    private String uri;
    private String method;
    private Map<String, String[]> parameters = new HashMap<>();

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

    public String getParameter(String name) {
        String[] arr = parameters.get(name);
        return (arr == null) ? null : arr[0];
    }

    public String[] getParameterValues(String name) {
        String[] arr = parameters.get(name);
        return (arr == null) ? null : Arrays.copyOf(arr, arr.length);
    }

    public Map<String, String[]> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void addParameter(String name, String value) {
        if (!parameters.containsKey(name)) {
            String[] arr = new String[1];
            arr[0] = value;
            parameters.put(name, arr);
        } else {
            String[] arr = parameters.get(name);
            arr = Arrays.copyOf(arr, arr.length+1);
            arr[arr.length-1] = value;
            parameters.put(name, arr);
        }
    }
}
