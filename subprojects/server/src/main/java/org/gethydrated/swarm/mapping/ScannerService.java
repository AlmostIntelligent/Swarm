package org.gethydrated.swarm.mapping;

import org.gethydrated.hydra.api.HydraException;
import org.gethydrated.hydra.api.service.SID;
import org.gethydrated.hydra.api.service.ServiceActivator;
import org.gethydrated.hydra.api.service.ServiceContext;
import org.jboss.vfs.*;
import org.jboss.vfs.util.FilterVirtualFileVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;

/**
 *
 */
public class ScannerService extends TimerTask implements ServiceActivator {

    private Logger logger = LoggerFactory.getLogger(ScannerService.class);
    private Timer timer = new Timer();
    private TempFileProvider provider;
    private VirtualFile deploy;
    private VirtualFile webapps;
    private Map<VirtualFile, SID> deployed = new HashMap<>();
    private ServiceContext context;

    @Override
    public void start(ServiceContext context) throws Exception {
        this.context = context;
        provider = TempFileProvider.create("tmp",Executors.newScheduledThreadPool(2));
        deploy = VFS.getChild(System.getProperty("swarm.deploy.dir"));
        if (!deploy.exists() || !deploy.isDirectory()) {
            throw new IllegalArgumentException("Illegal deployment dir: '" + deploy + "'");
        }
        webapps = VFS.getChild(System.getProperty("swarm.webapp.dir"));
        if (!webapps.exists() || ! webapps.isDirectory()) {
            throw new IllegalArgumentException("Illegal webapp dir: '" + webapps + "'");
        }
        timer.scheduleAtFixedRate(this, 0, 60000);
    }

    @Override
    public void stop(ServiceContext context) throws Exception {
        timer.cancel();
    }

    @Override
    public void run() {
        checkDeployDir();
        checkWebappDir();
        checkDeployed();
    }

    private void checkDeployDir() {
        FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(new VirtualFileFilter() {
            @Override
            public boolean accepts(VirtualFile file) {
                return (file.isDirectory() || file.getName().endsWith(".war"));
            }
        }, VisitorAttributes.DEFAULT);
        try {
            deploy.visit(visitor);
            for (VirtualFile f : visitor.getMatched()) {
                Closeable handle = null;
                if (!f.isDirectory()) {
                    handle = VFS.mountZip(f, f, provider);
                }
                if (f.getChild("WEB-INF/web.xml").exists()); {
                    copyWebapp(f);
                }
                if (handle != null) {
                    handle.close();
                }
            }
        } catch (IOException e) {
            logger.warn("Error: {}", e.getMessage());
        }
    }

    private void checkWebappDir() {
        FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(new VirtualFileFilter() {
            @Override
            public boolean accepts(VirtualFile file) {
                return (file.isDirectory());
            }
        }, VisitorAttributes.DEFAULT);
        try {
            webapps.visit(visitor);
            for (VirtualFile file : visitor.getMatched()) {
                if (file.getChild("local/deploy").exists()) {
                    deploy(file.getChild("local"));
                } else {
                    for (VirtualFile f : file.getChildren()) {
                        if (f.isDirectory() && f.getChild("deploy").exists()) {
                            deploy(f);
                        }
                    }
                }
            }
        } catch (IOException | HydraException e) {
            logger.warn("Error: {}", e.getMessage());
        }
    }

    private void checkDeployed() {
        for (VirtualFile f : new HashMap<>(deployed).keySet()) {
            if (f.getChild("error").exists()) {
                deployed.remove(f);
            }
            if (f.getChild("redeploy").exists()) {
                try {
                    undeploy(f);
                    deploy(f);
                    f.getChild("redeploy").getPhysicalFile().delete();
                } catch (IOException | HydraException e) {
                    logger.warn("Error: {}", e.getMessage());
                }
            }
            if (!f.getChild("deploy").exists()) {
                try {
                    undeploy(f);
                } catch (HydraException e) {
                    logger.warn("Error: {}", e.getMessage());
                }
            }
        }
    }

    private synchronized void deploy(VirtualFile file) throws HydraException, IOException {
        if (!deployed.keySet().contains(file) && !file.getChild("error").exists()) {
            SID webapp = context.startService("swarm::webapp");
            webapp.tell(file.getPhysicalFile().toString(), context.getSelf());
            deployed.put(file, webapp);
            logger.info("Web app {} deployed", file);
        }
    }

    private synchronized void undeploy(VirtualFile file) throws HydraException {
        if (deployed.keySet().contains(file)) {
            context.stopService(deployed.get(file));
            deployed.remove(file);
            logger.info("Web app {} undeployed", file);
        }
    }

    private void copyWebapp(VirtualFile file) throws IOException {
        VirtualFile targetDir = webapps.getChild(file.getName());
        if (!targetDir.exists()) {
            targetDir.getPhysicalFile().mkdir();
        }
        UUID id = UUID.randomUUID();
        VirtualFile idDir = targetDir.getChild(id.toString());
        while (!idDir.getPhysicalFile().mkdir()) {
            id = UUID.randomUUID();
            idDir = targetDir.getChild(id.toString());
        }
        VFSUtils.recursiveCopy(file, idDir);
        VFSUtils.recursiveDelete(file);
        idDir.getChild("deploy").getPhysicalFile().createNewFile();
    }
}
