package org.gethydrated.swarm.example.nettyservlets;

import org.gethydrated.swarm.container.WebAppContainer;
import org.gethydrated.swarm.container.core.StandardWebAppContainer;
import org.gethydrated.swarm.container.core.WebAppContext;
import org.gethydrated.swarm.server.ServerService;

/**
 *
 */
public class Main {

    public static void main(String[] args) throws Exception {
        WebAppContext conf = createContext();
        WebAppContainer webapp = new StandardWebAppContainer();
        webapp.init();
        webapp.start();
        ServerService s = new ServerService();
        s.setInitializer(new NettyServletInitializer(webapp));
        s.start();
        Thread.sleep(20000);
        s.stop();
        webapp.stop();
        webapp.destroy();
    }

    private static WebAppContext createContext() {
        WebAppContext ctx = new WebAppContext();
        return ctx;
    }
}
