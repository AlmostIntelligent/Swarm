package org.gethydrated.swarm.container;

import java.util.EventObject;

/**
 *
 */
public class LifecycleEvent extends EventObject {

    private final Object data;
    private final String type;

    public LifecycleEvent(Lifecycle lifecycle, String type, Object o) {
        super(lifecycle);
        this.type = type;
        this.data = o;
    }

    public Lifecycle getLifecycle() {
        return (Lifecycle) getSource();
    }

    public String getType() {
        return type;
    }
}
