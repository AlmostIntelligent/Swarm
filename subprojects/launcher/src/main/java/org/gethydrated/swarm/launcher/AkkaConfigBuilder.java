package org.gethydrated.swarm.launcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AkkaConfigBuilder {
	
	public static final Config build(String[] args) throws IOException {
		HashMap<String, Object> cfg = new HashMap<>();
		cfg.put("roles", new LinkedList<String>());
		parseArgs(args, cfg);			
		//validate(cfg);
		return fillCfg(cfg);
	}
	
	@SuppressWarnings("unchecked")
	private static void parseArgs(String[] args, HashMap<String, Object> cfg) {
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-p":
				if (args.length-1 > i) {
					try {
						int port = Integer.parseInt(args[i+1]);
						cfg.put("port", port);
						i++;
					} catch (NumberFormatException e) {
						cfg.put("port", readPort());
					}
				} else {
					cfg.put("port", readPort());
				}
				break;
			case "-r":
				if (args.length-1 > i) {
					((List<String>)cfg.get("roles")).add(args[i+1]);
					i++;
				} else {
					Main.printUsage(System.err);
					SystemExit.exit(ExitCode.FAIL);	
				}
				break;
			case "-hn":
				if (args.length-1 > i) {
					cfg.put("hostname", args[i+1]);
					i++;
				} else {
					Main.printUsage(System.err);
					SystemExit.exit(ExitCode.FAIL);	
				}
				break;
			default:
				Main.printUsage(System.err);
				SystemExit.exit(ExitCode.FAIL);
			}
		}
	}

	private static int readPort() {
		int port = -1;
		Scanner scanner = new Scanner(new UnCloseableInputStream(System.in));
		while (port < 0) {
			System.out.println("Please enter valid port number:");
			try {
				port = scanner.nextInt();
			} catch (Exception e) {
				scanner.next();
			}
		}
		scanner.close();
		return port;
	}

	public static final void validate(Map<String, Object> cfg) {
		checkNotNull("hostname",cfg);
		checkNotNull("port",cfg);
	}
	
	@SuppressWarnings("unchecked")
	public static final Config fillCfg(Map<String, Object> cfg) throws IOException {
		Map<String, Object> props = new HashMap<>();
		if (cfg.containsKey("hostname")) {
			props.put("akka.remote.netty.tcp.hostname", cfg.get("hostname"));
		}
		if (cfg.containsKey("port")) {
			props.put("akka.remote.netty.tcp.port", cfg.get("port"));
		}
		if (!((List<String>)cfg.get("roles")).isEmpty()) {
			props.put("akka.cluster.roles", cfg.get("roles"));
		}
		VirtualFile homeDir = VFS.getChild(System.getProperty("swarm.home.dir"));
		VirtualFile cfgfile = (System.getProperty("swarm.conf.dir") == null) ? homeDir.getChild("conf") : VFS.getChild(System.getProperty("swarm.conf.dir"));
		return ConfigFactory.parseMap(props).withFallback(ConfigFactory.parseFile(cfgfile.getChild("swarm.conf").getPhysicalFile()));	
	}
	
	private static final void checkNotNull(String key, Map<String, Object> map) {
		if (!map.containsKey(key)) {
			throw new RuntimeException("Config validation error: " + key + " not found!");
		}
	}
}
