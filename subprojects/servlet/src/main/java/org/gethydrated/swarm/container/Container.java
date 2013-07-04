package org.gethydrated.swarm.container;

import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public interface Container extends Lifecycle {

    public Logger getLogger();

    public String getName();

    public Container getParent();

    public void setParent(Container container);

    public void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
