package org.gethydrated.swarm.container;

import java.util.List;

/**
 *
 */
public interface Lifecycle {

    public void addListener(LifecycleListener listener);

    public List<LifecycleListener> getListeners();

    public void removeListener(LifecycleListener listener);

    public void init();

    public void start();

    public void stop();

    public void destroy();

    public LifecycleState getState();

    public String getStateName();

}
