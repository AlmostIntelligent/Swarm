package org.gethydrated.swarm.deploy;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FilterDescriptor {
    private String name;
    private String className;
    private Map<String, String> params = new HashMap<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setClassName(String aClass) {
        this.className = aClass;
    }

    public String getClassName() {
        return className;
    }

    public void addInitParameter(String paramName, String paramValue) {
        params.put(paramName, paramValue);
    }

    public Map<String, String> getInitParameters() {
        return params;
    }

    @Override
    public String toString() {
        return "FilterDescriptor{" +
                "name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", params=" + params +
                '}';
    }


}
