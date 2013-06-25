package org.gethydrated.swarm.container.core;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.gethydrated.swarm.container.Container;
import org.gethydrated.swarm.container.LifecycleListener;
import org.gethydrated.swarm.container.LifecycleState;
import org.gethydrated.swarm.container.WebAppContainer;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class StandardWebAppContainer implements WebAppContainer {
    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public String getDomain() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public Container getParent() {
        return null;
    }

    @Override
    public void setParent(Container container) {

    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) throws ServletException, IOException {

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
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public LifecycleState getState() {
        return null;
    }

    @Override
    public String getStateName() {
        return null;
    }
}
