package org.gethydrated.swarm.core.messages.deploy;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ServletDescriptor implements Comparable<ServletDescriptor> {
    private int loadOnStartup;
    private String className;
    private String name;
    private Map<String, String> params = new HashMap<>();

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addInitParameter(String paramName, String paramValue) {
        params.put(paramName, paramValue);
    }

    public Map<String, String> getInitParameters() {
        return params;
    }

    @Override
    public String toString() {
        return "ServletDescriptor{" +
                "loadOnStartup=" + loadOnStartup +
                ", className='" + className + '\'' +
                ", name='" + name + '\'' +
                ", params=" + params +
                '}';
    }

	@Override
	public int compareTo(ServletDescriptor o) {
		if (o.getLoadOnStartup() == 0 && this.getLoadOnStartup() == 0) {
			return 0;
		}
		if (this.getLoadOnStartup() == 0) {
			return 1;
		}
		if (o.getLoadOnStartup() == 0) {
			return -1;
		}
		return Integer.compare(this.loadOnStartup, o.loadOnStartup);
	}


}
