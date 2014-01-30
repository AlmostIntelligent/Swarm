package org.gethydrated.swarm.core.servlets;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.gethydrated.swarm.core.messages.container.ApplicationFilterChain;
import org.gethydrated.swarm.core.messages.container.Beacon.WebAppBeacon;
import org.gethydrated.swarm.core.messages.container.Beacon.StartBeacon;
import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;
import org.gethydrated.swarm.core.servlets.container.AbstractContainer;
import org.gethydrated.swarm.core.servlets.container.ApplicationContext;
import org.gethydrated.swarm.core.servlets.container.ApplicationContextFactory;
import org.gethydrated.swarm.core.servlets.container.FilterContainer;
import org.gethydrated.swarm.core.servlets.container.FilterWorker;
import org.gethydrated.swarm.core.servlets.container.LifecycleState;
import org.gethydrated.swarm.core.servlets.container.ServletContainer;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.routing.SmallestMailboxRouter;

public class WebApp extends AbstractContainer {
	
	ActorRef mediator;
	Cancellable timertask;
	String ctxName;
	ApplicationContext context;
	private VirtualFile handle;
    private TempFileProvider provider;
    private Closeable vfsmount;
	private ActorRef filters;
    
	public WebApp(String base, ActorRef parent) {
		super(base, parent);
	}
	
	@Override
	public void onReceive(Object o) throws Exception {
		if(o instanceof StartBeacon) {
			mediator.tell(new DistributedPubSubMediator.Publish("webapp-discovery", new WebAppBeacon(ctxName)), self());
		} else if (o instanceof SwarmHttpRequest && getState() == LifecycleState.RUNNING) {
			getLogger().info("{}", o);
			SwarmHttpRequest request = (SwarmHttpRequest) o;
	        SwarmHttpResponse response = new SwarmHttpResponse();
	        response.setRequestId(request.getRequestId());
	        response.setHttpVersion(request.getHttpVersion());
	        try {
	        	ApplicationFilterChain chain = context.createFilterChain(request, response, sender());
	        	if (chain != null) {
	        		response.setStatus(200);
	        		filters.tell(chain, self());
	        	} else {
	        		sender().tell(response, self());
	        	}
	        } catch (RuntimeException e) {
	        	e.printStackTrace();
	        	response.setStatus(404);
	        	response.setContentType("text/plain");
	        	response.setContent("The page you are looking for does not exist. Error 404.");
	        	sender().tell(response, self());
	        }
		} else {
			unhandled(o);
		}
	}

	@Override
	protected void doInit() throws Exception {
		provider = TempFileProvider.create("tmp",Executors.newScheduledThreadPool(2));
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
		try {
			context = ApplicationContextFactory.create(ctxName, handle, getLogger());
			context.setActorSystem(context().system());
			context.setLogger(getLogger());
			context.init();
			createFilters();
			createServlets();
			
			
			mediator = DistributedPubSubExtension.get(context().system()).mediator();
			timertask = context().system().scheduler().schedule(new FiniteDuration(0, TimeUnit.SECONDS), new FiniteDuration(1, TimeUnit.SECONDS), 
					self(), new StartBeacon(), context().system().dispatcher(), self());
		} catch (Throwable t) {
			getLogger().error(t, "Could not start Web app '" + getName() + "'");
			VirtualFile error = handle.getParent().getChild("error");
			error.getPhysicalFile().createNewFile();
			error.openStream();
			PrintStream p = new PrintStream(error.getPhysicalFile());
			t.printStackTrace(p);
			p.close();
			throw t;
		}
	}

	@Override
	protected void doDestroy() throws IOException {
		destroyFilters();
		destroyServlets();
		context.destroy();
		if (timertask != null) {
			timertask.cancel();
		}
		if (vfsmount != null) {
			vfsmount.close();
		}
		provider.close();
	}
	
	private void createServlets() {
		for (String s : context.getServletRegistrations().keySet()) {
			ActorRef ref = context().actorOf(Props.create(ServletContainer.class, context, s, self()));
			context.getServletFacade(s).setRef(ref);
		}
	}

	private void destroyServlets() {
		for (String s : context.getServletRegistrations().keySet()) {
			context().stop(context.getServletFacade(s).ref());
		}
	}
	
	private void createFilters() {
		filters = context().actorOf(Props.create(FilterWorker.class, context).withRouter(new SmallestMailboxRouter(5)), "filters");
		for (String s : context.getFilterRegistrations().keySet()) {
			context.getLogger().info("Init filter {}", s);
			FilterContainer f = context.getFilter(s);
			f.init();
		}
	}
	
	private void destroyFilters() {
		for (String s : context.getFilterRegistrations().keySet()) {
			context.getLogger().info("Destroy filter {}", s);
			FilterContainer f = context.getFilter(s);
			f.destroy();
		}
	}
}
