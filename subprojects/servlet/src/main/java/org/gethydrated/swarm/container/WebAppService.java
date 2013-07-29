package org.gethydrated.swarm.container;


import org.gethydrated.hydra.api.service.MessageHandler;
import org.gethydrated.hydra.api.service.SID;
import org.gethydrated.hydra.api.service.ServiceActivator;
import org.gethydrated.hydra.api.service.ServiceContext;
import org.gethydrated.swarm.container.core.ApplicationContext;
import org.gethydrated.swarm.container.core.ApplicationContextFactory;
import org.gethydrated.swarm.mapping.MappingService;
import org.gethydrated.swarm.messages.RegisterApp;
import org.gethydrated.swarm.server.SwarmHttpRequest;
import org.gethydrated.swarm.server.SwarmHttpResponse;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.util.concurrent.Executors;

/**
 *
 */
public class WebAppService implements ServiceActivator {

    private ApplicationContext application;
    private VirtualFile handle;
    private TempFileProvider provider;
    private Closeable vfsmount;
    private ServiceContext context;
    private Logger logger = LoggerFactory.getLogger(WebAppService.class);

    public void init(String path) {
        try {
            VirtualFile file = VFS.getChild(path);
            String ctxName = file.getParent().getName();
            if (file.getChild(ctxName).exists() && file.getChild(ctxName).isDirectory()) {
                handle = file.getChild(ctxName);
            } else if (file.getChild(ctxName+".war").exists() && !file.getChild(ctxName+".war").isDirectory()) {
                handle = file.getChild(ctxName+".war");
                vfsmount = VFS.mountZip(handle, handle, provider);
            } else {
                throw new FileNotFoundException(ctxName);
            }
            System.out.println(handle);
            application = ApplicationContextFactory.create(handle);
            application.init();
            application.start();
            SID mapper = context.getLocalService(MappingService.MAPPER_NAME);
            System.out.println(application.getContextPath());
            RegisterApp reg = new RegisterApp();
            reg.setContextName((application.getContextPath().startsWith("/")) ? application.getContextPath().substring(1) : application.getContextPath());
            mapper.tell(reg, context.getSelf());
        } catch (Exception e) {
            logger.error("Error:", e);
            try {
                handle.getParent().getChild("error").getPhysicalFile().createNewFile();
                context.stopService(context.getSelf());
            } catch (Exception e1) {
                logger.error("Error: {}", e1.getMessage());
            }
        }
    }

    @Override
    public void start(ServiceContext context) throws Exception {
        this.context = context;
        provider = TempFileProvider.create("tmp",Executors.newScheduledThreadPool(2));
        context.registerMessageHandler(String.class, new MessageHandler<String>() {
            @Override
            public void handle(String message, SID sender) {
                init(message);
            }
        });
        context.registerMessageHandler(SwarmHttpRequest.class, new MessageHandler<SwarmHttpRequest>() {
            @Override
            public void handle(SwarmHttpRequest message, SID sender) {
                invoke(message, sender);
            }
        });
    }

    @Override
    public void stop(ServiceContext context) throws Exception {
        if (application != null) {
            application.stop();
            application.destroy();
        }
        if (vfsmount != null) {
            vfsmount.close();
        }
    }

    public void invoke(SwarmHttpRequest request, SID sender) {
        SwarmHttpResponse response = new SwarmHttpResponse();
        application.invoke(request, response);
        sender.tell(response, context.getSelf());
    }


}
