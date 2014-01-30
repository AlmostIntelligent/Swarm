package org.gethydrated.swarm.core.servlets;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.FilterVirtualFileVisitor;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.actor.Scheduler;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import org.gethydrated.swarm.core.messages.deploy.Deployment.Deploy;
import org.gethydrated.swarm.core.messages.deploy.Deployment.Undeploy;

public class WebAppScanner extends UntypedActor {

	private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	private Scheduler scheduler = context().system().scheduler();
	private Cancellable timertask;
	private TempFileProvider provider;
	private VirtualFile deploy;
	private VirtualFile webapps;
	private List<VirtualFile> deployed = new LinkedList<>();
	
	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof String && o.equals("Tick")) {
			checkDeployDir();
			checkWebappDir();
			checkDeployed();
		}
	}
	
	private void checkDeployed() {
        for (VirtualFile f : deployed) {
            if (f.getChild("error").exists()) {
                deployed.remove(f);
            }
            if (f.getChild("redeploy").exists()) {
                try {
                    undeploy(f);
                    deploy(f);
                    f.getChild("redeploy").getPhysicalFile().delete();
                } catch (IOException e) {
                    logger.warning("Error: {}", e.getMessage());
                }
            }
            if (!f.getChild("deploy").exists()) {
                try {
                    undeploy(f);
                } catch (IOException e) {
                    logger.warning("Error: {}", e.getMessage());
                }
            }
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
        } catch (IOException e) {
            logger.warning("Error: {}", e.getMessage());
        }
	}

	private void deploy(VirtualFile file) throws IOException {
        if (!deployed.contains(file) && !file.getChild("error").exists()) {
        	ActorSelection webapps = context().actorSelection("/user/webapps");
            webapps.tell(new Deploy(file.getPhysicalFile().toString()), self());
            deployed.add(file);
            logger.info("Web app {} deployed", file);
        }
	}
	
	private void undeploy(VirtualFile file) throws IOException {
		if (deployed.contains(file)) {
			ActorSelection webapps = context().actorSelection("/user/webapps");
	        webapps.tell(new Undeploy(file.getPhysicalFile().toString()), self());
	        deployed.remove(file);
	        logger.info("Web app {} undeployed", file);
		}
	}

	private void checkDeployDir() {
        FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(new VirtualFileFilter() {
            @Override
            public boolean accepts(VirtualFile file) {
                return (file.isDirectory() || file.getName().endsWith(".war"));
            }
        }, VisitorAttributes.DEFAULT);
        Closeable handle = null;
        try {
            deploy.visit(visitor);
            for (VirtualFile f : visitor.getMatched()) {
            	handle = null;
                File real = f.getPhysicalFile();
                if (!f.isDirectory()) {
                    handle = VFS.mountZip(f, f, provider);
                }
                if (f.getChild("WEB-INF/web.xml").exists()); {
                    copyWebapp(f, real);
                    if(!real.delete()) {
                    	logger.error("Could not delete " + real);
                    }
                }
                if (handle != null) {
                    handle.close();
                }
            }
        } catch (IOException e) {
            logger.warning("Error: {}", e.getMessage());
        } finally {
        	if (handle != null) {
        		try {
					handle.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
	}

	private void copyWebapp(VirtualFile f, File real) throws IOException {
		String name = f.getName();
		if (name.endsWith(".war")) {
			name = name.substring(0, name.length()-4);
		}
        VirtualFile targetDir = webapps.getChild(name);
        if (!targetDir.exists()) {
            targetDir.getPhysicalFile().mkdir();
        }
        UUID id = UUID.randomUUID();
        VirtualFile idDir = targetDir.getChild(id.toString());
        while (!idDir.getPhysicalFile().mkdir()) {
            id = UUID.randomUUID();
            idDir = targetDir.getChild(id.toString());
        }
        Files.copy(real.toPath(), idDir.getChild(f.getName()).getPhysicalFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        idDir.getChild("deploy").getPhysicalFile().createNewFile();
	}

	@Override
	public void preStart() throws IOException {
		provider = TempFileProvider.create("tmp",Executors.newScheduledThreadPool(2));
		deploy = VFS.getChild(System.getProperty("swarm.deploy.dir"));
		if (!deploy.exists() || !deploy.isDirectory()) {
            throw new IllegalArgumentException("Illegal deployment dir: '" + deploy + "'");
        }
        webapps = VFS.getChild(System.getProperty("swarm.webapp.dir"));
        if (!webapps.exists() || ! webapps.isDirectory()) {
            throw new IllegalArgumentException("Illegal webapp dir: '" + webapps + "'");
        }
        timertask = scheduler.schedule(new FiniteDuration(0, TimeUnit.MILLISECONDS), new FiniteDuration(1000, TimeUnit.MILLISECONDS), self(), "Tick", 
        		context().system().dispatcher(), null);
	}

	@Override
	public void postStop() throws IOException {
		timertask.cancel();
		provider.close();
	}
}
