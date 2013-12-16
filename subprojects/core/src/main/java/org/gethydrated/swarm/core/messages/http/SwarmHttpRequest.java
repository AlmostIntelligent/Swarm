package org.gethydrated.swarm.core.messages.http;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class SwarmHttpRequest extends BaseHttpMessage implements HttpRequest, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4029454995126548651L;
    private String uri;
    private String method;
    private Map<String, String[]> parameters = new HashMap<>();
    private Map<String, Object> attributes = new HashMap<>();
    
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

    public SwarmHttpRequest addParameter(String name, String value) {
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
        return this;
    }

    public SwarmHttpRequest addParameters(Map<String, List<String>> parameters) {
        for (Entry<String, List<String>> e : parameters.entrySet()) {
            for (String s : e.getValue()) {
                addParameter(e.getKey(), s);
            }
        }
        return this;
    }
    


	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}
	
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

    @Override
    public String toString() {
        return "SwarmHttpRequest{" +
                "uri='" + uri +
                "', method='" + method +
                "', parameters='" + parameters + '\'' +
                ", " + super.toString() + "}";
    }
}
