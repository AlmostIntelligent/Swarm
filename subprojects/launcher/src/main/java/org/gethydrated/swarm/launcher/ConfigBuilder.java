package org.gethydrated.swarm.launcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigBuilder {
	
	public static final Config build() {
		Map<String, Object> props = new HashMap<>();
		props.put("akka.actor.provider", "akka.cluster.ClusterActorRefProvider");
		props.put("akka.remote.log-remote-lifecycle-events", "off");
		props.put("akka.remote.netty.tcp.hostname", "127.0.0.1");
		props.put("akka.remote.netty.tcp.port", "2552");
		List<String> seeds = new LinkedList<>();
		seeds.add("akka.tcp://ClusterSystem@127.0.0.1:2552");
		props.put("akka.cluster.seed-nodes", seeds);
		return ConfigFactory.parseMap(props);
		
	}
}
