package org.gethydrated.swarm.core.messages.deploy;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class Mapping {

    private String name;
    private List<String> patterns = new LinkedList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPattern(String name) {
        patterns.add(name);
    }

    public String[] getPatterns() {
        return patterns.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "name='" + name + '\'' +
                ", patterns=" + patterns +
                '}';
    }
}
