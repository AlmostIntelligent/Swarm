package org.gethydrated.swarm.core.servlets.container;

import java.io.FileNotFoundException;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public abstract class AbstractContainer extends UntypedActor implements Container{

	private final String name;
	
	private ActorRef parent;
	
	private LifecycleState state = LifecycleState.CREATED;

	private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	
	public AbstractContainer(String name, ActorRef parent) {
		this.name = name;
		this.parent = parent;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public ActorRef getParent() {
		return parent;
	}
	
	@Override
	public void setParent(ActorRef parent) {
		this.parent = parent;
	}
	
	@Override
    public final void init() {
        if (state == LifecycleState.CREATED) {
            setState(LifecycleState.INIT);
            try {
                doInit();
                setState(LifecycleState.RUNNING);
            } catch (Throwable t) {
                setState(LifecycleState.FAILED);
                getLogger().warning("Error: {}", t);
                t.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Context already initialized.");
        }
    }
	
	@Override
    public final void destroy() {
        if (state == LifecycleState.RUNNING || state == LifecycleState.FAILED) {
            setState(LifecycleState.DESTROY);
            try {
                doDestroy();
            } catch (Throwable t) {
                //TODO: handle error correctly
                throw new RuntimeException(t);
            } finally {
                setState(LifecycleState.STOPPED);
            }
        } else {
            throw  new IllegalStateException("Context not yet stopped.");
        }
    }
	
	protected abstract void doInit() throws Exception;
	
	protected abstract void doDestroy() throws Exception;

	@Override
    public LifecycleState getState() {
        return state;
    }

    @Override
    public String getStateName() {
        return state.toString();
    }
    
    protected void setState(LifecycleState state) {
        this.state = state;
    }
    
    @Override
    public void preStart() throws Exception {
    	init();
    }
    
    @Override
    public void postStop() throws Exception {
    	destroy();
    }
    
	@Override
	public LoggingAdapter getLogger() {
		return logger;
	}
}
