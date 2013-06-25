package org.gethydrated.swarm.container;

/**
 *
 */
public enum LifecycleState {
    CREATED,
    INIT,
    POST_INIT,
    PRE_START,
    START,
    POST_START,
    PRE_STOP,
    STOP,
    POST_STOP,
    DESTROY,
    FINISHED,
    FAILED
}
