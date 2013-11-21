package org.gethydrated.swarm.core.servlets;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.gethydrated.swarm.core.messages.container.Beacon.WebAppBeacon;
import org.gethydrated.swarm.core.messages.container.Beacon.StartBeacon;
import org.gethydrated.swarm.core.messages.http.BaseHttpMessage;
import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;
import org.gethydrated.swarm.core.servlets.container.AbstractContainer;
import org.gethydrated.swarm.core.servlets.container.ApplicationContext;
import org.gethydrated.swarm.core.servlets.container.ApplicationContextFactory;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;

public class WebApp extends AbstractContainer {
	
	ActorRef mediator;
	Cancellable timertask;
	String ctxName;
	ApplicationContext context;
	private VirtualFile handle;
    private TempFileProvider provider;
    private Closeable vfsmount;
    
	public WebApp(String base, ActorRef parent) {
		super(base, parent);
		System.out.println(base);
	}
	
	@Override
	public void onReceive(Object o) throws Exception {
		if(o instanceof StartBeacon) {
			mediator.tell(new DistributedPubSubMediator.Publish("webapp-discovery", new WebAppBeacon(ctxName)), self());
		} else if (o instanceof SwarmHttpRequest) {
	        SwarmHttpResponse response = new SwarmHttpResponse();
	        response.setRequestId(((BaseHttpMessage) o).getRequestId());
	        response.setHttpVersion(((BaseHttpMessage) o).getHttpVersion());
	        response.setStatus(200);
	        response.setContent("Grettings from " + ctxName + " unfortunatly i am not ready yet to serve content.");
	        response.setContentType("text/plain");
	        sender().tell(response,sender());
		} else {
			unhandled(o);
		}
	}

	@Override
	protected void doInit() throws Exception {
		VirtualFile file = VFS.getChild(getName());
		ctxName = file.getParent().getName();
		getLogger().info("{}", ctxName);
		if (file.getChild(ctxName).exists() && file.getChild(ctxName).isDirectory()) {
            handle = file.getChild(ctxName);
        } else if (file.getChild(ctxName+".war").exists() && !file.getChild(ctxName+".war").isDirectory()) {
            handle = file.getChild(ctxName+".war");
            vfsmount = VFS.mountZip(handle, handle, provider);
        } else {
            throw new FileNotFoundException(ctxName);
        }
		context = ApplicationContextFactory.create(handle);
		context.setActorSystem(context().system());
		context.setRootContext(getContext());
		context.setRootRef(getSelf());
		context.setLogger(getLogger());
		createFilters();
		createServlets();
		
		
		mediator = DistributedPubSubExtension.get(context().system()).mediator();
		timertask = context().system().scheduler().schedule(new FiniteDuration(0, TimeUnit.SECONDS), new FiniteDuration(1, TimeUnit.SECONDS), 
				self(), new StartBeacon(), context().system().dispatcher(), self());
	}

	@Override
	protected void doDestroy() throws IOException {
		if (timertask != null) {
			timertask.cancel();
		}
		if (vfsmount != null) {
			vfsmount.close();
		}
	}
	
	private void createServlets() {
		// TODO Auto-generated method stub
		
	}

	private void createFilters() {
		// TODO Auto-generated method stub
		
	}
}
