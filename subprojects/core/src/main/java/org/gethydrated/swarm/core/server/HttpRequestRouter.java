package org.gethydrated.swarm.core.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletException;

import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;
import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import org.gethydrated.swarm.core.messages.container.Beacon.WebAppBeacon;

public class HttpRequestRouter extends UntypedActor {

	private Map<String, LinkedList<ActorRef>> mappings = new HashMap<>();
	
	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof SwarmHttpRequest) {
			map((SwarmHttpRequest) o);
		} else if (o instanceof WebAppBeacon) {
			addRoutee(((WebAppBeacon) o).getCtxName());
		} else if (o instanceof Terminated) {
			deleteRoutee(((Terminated) o).actor());
		} else {
			unhandled(o);
		}
	}
	
	private void deleteRoutee(ActorRef actor) {
		for (LinkedList<ActorRef> l : mappings.values()) {
			l.remove(actor);
		}
	}

	private boolean containsRoutee(ActorRef actor) {
		for (LinkedList<ActorRef> l : mappings.values()) {
			if (l.contains(actor)) {
				return true;
			}
		}
		return false;
	}
	
	private void addRoutee(String ctxName) {
		if (!containsRoutee(sender())) {
			if(mappings.containsKey(ctxName)) {
				LinkedList<ActorRef> l = mappings.get(ctxName);
				l.add(sender());
			} else {
				LinkedList<ActorRef> l = new LinkedList<>();
				l.add(sender());
				mappings.put(ctxName, l);
			}
			context().watch(sender());
			log.info("New webapp {} found at {}", ctxName, sender());
		}
	}

	public void map(SwarmHttpRequest request) throws Exception {
		log.info("New request: {}", request);
        String uri = request.getUri();
        String idx;
        if (uri.equals("/") || !uri.substring(1).contains("/")) {
            idx = uri.substring(1);
        } else {
            idx = uri.substring(1, uri.indexOf("/", 1));
        }
        if (idx == "") {
            idx = "ROOT";
        }
        LinkedList<ActorRef> app = mappings.get(idx);
        if (app == null || app.isEmpty()) {
            invoke404(request);
        } else {
        	int i = (int)(Math.random() * ((app.size()-1) + 1));
            app.get(i).tell(request, sender());
        }
	}
	
	public void invoke404(SwarmHttpRequest request) throws ServletException, IOException {
        SwarmHttpResponse response = new SwarmHttpResponse();
        response.setRequestId(request.getRequestId());
        response.setHttpVersion(request.getHttpVersion());
        response.setStatus(404);
        response.setContent("Webapp not found");
        response.setContentType("text/plain");
        sender().tell(response,sender());
    }
	
	@Override
	public void preStart() {
		ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
		mediator.tell(new DistributedPubSubMediator.Subscribe("webapp-discovery", getSelf()), getSelf());
	}

}
