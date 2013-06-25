package org.gethydrated.swarm.container;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 *
 */
public interface Container extends Lifecycle {

    public Logger getLogger();

    public String getDomain();

    public String getName();

    public void setName(String name);

    public Container getParent();

    public void setParent(Container container);

    public void invoke(HttpRequest request, HttpResponse response) throws ServletException, IOException;
}
