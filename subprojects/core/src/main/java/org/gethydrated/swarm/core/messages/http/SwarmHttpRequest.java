package org.gethydrated.swarm.core.messages.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.DispatcherType;

import org.gethydrated.swarm.core.SObject;
import org.gethydrated.swarm.core.servlets.container.ApplicationContext;

public class SwarmHttpRequest extends BaseHttpMessage implements HttpRequest, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4029454995126548651L;
    private String uri;
    private String method;
    private Map<String, String[]> parameters = new HashMap<>();
    private transient Map<String, Object> attributes = new HashMap<>();
    private Map<String, SObject> serializables = new HashMap<>();
	private String requestURL;
	private String servletPath;
	private String pathInfo;
	private String contextPath;
	private DispatcherType dispatcherType;
    
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


	public String getURL() {
		return requestURL;
	}

	public StringBuffer getRequestURL() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("http://");
        buffer.append(getServerName());
        buffer.append(getRequestURI());
        return buffer;
    }
	
	public String getRequestURI() {
        String uri = getUri();
        int i = uri.indexOf("?");
        if (i > 0) {
            uri = uri.substring(0, i);
        }
		return uri;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
    
	public String getPathInfo() {
		return pathInfo;
	}

	public String getContextPath() {
		return contextPath;
	}

	public String getServletPath() {
		return servletPath;
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

	private void readObject(ObjectInputStream i) throws ClassNotFoundException, IOException {
		i.defaultReadObject();
		attributes = new HashMap<>();
	}
	
	private void writeObject(ObjectOutputStream o) 
            throws IOException, ClassNotFoundException
    {
        Iterator<Entry<String, Object>> iter = attributes.entrySet().iterator();
        while (iter.hasNext()) {
        	Entry<String, Object> e = iter.next();
        	if ((e.getValue() instanceof Serializable)) {
        		try {
	        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        		ObjectOutputStream    oos  = new ObjectOutputStream( baos );
	        		oos.writeObject( e.getValue() );
	        		oos.close();
	        		byte[] array = baos.toByteArray();
	        		serializables.put(e.getKey(), new SObject(e.getValue().getClass().getCanonicalName(), array));
        		} catch (Exception e1) {
        			System.out.println("Couldnt write " + e.getValue());
        		}
        	}
        }
        o.defaultWriteObject();
    }
	
	public void deserialize(ApplicationContext context) {
		for (Entry<String, SObject> e : serializables.entrySet()) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(e.getValue().getData());
				ObjectInputStream ois = new ContextAwareObjectInputStream(bais, context);
				Object o = ois.readObject();
				ois.close();
				attributes.put(e.getKey(), o);
			} catch (IOException | ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}
		serializables.clear();
	}
	
	public SwarmHttpRequest copy(ApplicationContext context) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		oos.close();
		ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ContextAwareObjectInputStream(bais, context);
		SwarmHttpRequest res;
		try {
			res = (SwarmHttpRequest) ois.readObject();
			return res;
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} finally {
			ois.close();
		}
	}
	
    @Override
    public String toString() {
        return "SwarmHttpRequest{" +
                "uri='" + uri +
                "', method='" + method +
                "', parameters='" + parameters + '\'' +
                ", " + super.toString() + "}";
    }

    private class ContextAwareObjectInputStream extends ObjectInputStream {
    	
    	private final ApplicationContext ctx;
    	
		public ContextAwareObjectInputStream(InputStream in, ApplicationContext ctx) throws IOException {
			super(in);
			this.ctx = ctx;
		}

		@Override
		public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			try {
				return ctx.getClassLoader().loadClass(desc.getName());
			} catch (Exception e) {
			}
			return super.resolveClass(desc);
		}
    }

	public void setDispatcherType(DispatcherType dispatcherType) {
		this.dispatcherType = dispatcherType;
	}
	
	public DispatcherType getDispatcherType() {
		return dispatcherType;
	}
    
}
