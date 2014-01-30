package org.gethydrated.swarm.core.servlets;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gethydrated.swarm.core.messages.deploy.Deployment.Deploy;
import org.gethydrated.swarm.core.messages.deploy.Deployment.Undeploy;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class WebAppRoot extends UntypedActor {

	private Map<String, ActorRef> webapps = new HashMap<>();
	private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	
	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof Deploy) {
			deploy((Deploy) o);
		} else if (o instanceof Undeploy) {
			undeploy((Undeploy) o);
		} else if (o instanceof Terminated) {
			webapps.values().removeAll(Collections.singleton(((Terminated) o).actor()));
			logger.info("{} terminated", o);
		} else {
			unhandled(o);
		}
	}

	private void undeploy(Undeploy base) {
		if (webapps.containsKey(base.getFile())) {
			ActorRef ref = webapps.get(base.getFile());
			context().stop(ref);
		} else {
			logger.warning("Webapp wasn't deployed: {}", base.getFile());
		}
	}

	private void deploy(Deploy base) throws IOException {
		if (!webapps.containsKey(base.getFile())) {
			VirtualFile file = VFS.getChild(base.getFile());
			String ctxName = file.getParent().getName();
			String id = file.getName();
			ActorRef ref = context().actorOf(Props.create(WebApp.class, base.getFile(), null), ctxName + "---" + id);
			context().watch(ref);
			webapps.put(base.getFile(), ref);
		} else {
			logger.warning("Webapp already deployed: {}", base.getFile());
		}
	}
}
