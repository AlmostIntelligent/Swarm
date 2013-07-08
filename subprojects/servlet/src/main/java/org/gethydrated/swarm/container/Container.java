package org.gethydrated.swarm.container;

import org.slf4j.Logger;

/**
 *
 */
public interface Container extends Lifecycle {

    public Logger getLogger();

    public String getName();

    public Container getParent();

    public void setParent(Container container);
}
