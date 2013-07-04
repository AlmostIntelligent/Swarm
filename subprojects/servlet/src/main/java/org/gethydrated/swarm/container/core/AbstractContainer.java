package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.container.Container;
import org.gethydrated.swarm.container.LifecycleListener;
import org.gethydrated.swarm.container.LifecycleState;

import java.util.List;

/**
 *
 */
public abstract class AbstractContainer implements Container {

    private final String name;

    private Container parent;

    private LifecycleState state = LifecycleState.CREATED;

    public AbstractContainer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public void setParent(Container parent) {
        this.parent = parent;
    }

    @Override
    public void addListener(LifecycleListener listener) {

    }

    @Override
    public List<LifecycleListener> getListeners() {
        return null;
    }

    @Override
    public void removeListener(LifecycleListener listener) {

    }

    @Override
    public final void init() {
        if (state == LifecycleState.CREATED) {
            setState(LifecycleState.INIT);
            try {
                doInit();
                setState(LifecycleState.READY);
            } catch (Throwable t) {
                setState(LifecycleState.FAILED);
            }
        } else {
            throw new IllegalStateException("Context already initialized.");
        }
    }



    @Override
    public final void start() {
        if (state == LifecycleState.READY) {
            setState(LifecycleState.START);
            try {
                doStart();
                setState(LifecycleState.RUNNING);
            } catch (Throwable t) {
                setState(LifecycleState.FAILED);
            }
        } else {
            throw new IllegalStateException("Context not initialized.");
        }
    }

    @Override
    public final void stop() {
        if (state == LifecycleState.RUNNING) {
            setState(LifecycleState.STOP);
            try {
                doStop();
                setState(LifecycleState.STOPPED);
            } catch (Throwable t) {
                setState(LifecycleState.FAILED);
            }
        } else {
            throw new IllegalStateException("Context not running.");
        }
    }

    @Override
    public final void destroy() {
        if (state == LifecycleState.STOPPED || state == LifecycleState.FAILED) {
            setState(LifecycleState.DESTROY);
            try {
                doDestroy();
            } catch (Throwable t) {
                //TODO: handle error correctly
                throw new RuntimeException(t);
            } finally {
                setState(LifecycleState.FINISHED);
            }
        } else {
            throw  new IllegalStateException("Context not yet stopped.");
        }
    }

    public abstract void doInit() throws Exception;

    public abstract void doStart() throws Exception;

    public abstract void doStop() throws Exception;

    public abstract void doDestroy() throws Exception;

    protected void setState(LifecycleState state) {
        this.state = state;
    }

    @Override
    public LifecycleState getState() {
        return state;
    }

    @Override
    public String getStateName() {
        return state.toString();
    }
}
