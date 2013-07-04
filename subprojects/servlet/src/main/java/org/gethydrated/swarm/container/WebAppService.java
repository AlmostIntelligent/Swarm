package org.gethydrated.swarm.container;


import org.gethydrated.swarm.container.core.ApplicationContext;
import org.gethydrated.swarm.container.core.ApplicationContextFactory;
import org.gethydrated.swarm.mapping.Invokable;
import org.gethydrated.swarm.server.ServerService;
import org.gethydrated.swarm.server.SwarmHttpRequest;
import org.gethydrated.swarm.server.SwarmHttpResponse;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import javax.servlet.ServletException;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;

/**
 *
 */
public class WebAppService implements Invokable {

    private ApplicationContext application;
    private ServerService server;
    private final VirtualFile handle;
    private Closeable vfsmount;

    public WebAppService(VirtualFile handle) throws IOException {
        if (handle.getName().endsWith(".war")) {
            TempFileProvider provider = TempFileProvider.create("tmp", Executors.newScheduledThreadPool(2));
            vfsmount = VFS.mountZip(handle, handle, provider);
        }
        this.handle = handle;
    }

    public void init() throws Exception {
        application = ApplicationContextFactory.create(handle);
        application.init();
    }

    public void start() throws Exception {

        application.start();
    }

    public void stop() throws Exception {
        application.stop();
        application.destroy();
        if (vfsmount != null) {
            vfsmount.close();
        }
        System.out.println("stopped");
    }

    public ApplicationContext getApplication() {
        return application;
    }

    public void setServer(ServerService server) {
        this.server = server;
    }

    public ServerService getServer() {
        return server;
    }

    @Override
    public void invoke(SwarmHttpRequest request) throws ServletException, IOException {
        SwarmHttpResponse response = new SwarmHttpResponse();
        application.invoke(request, response);
        server.send(response);
    }

}
