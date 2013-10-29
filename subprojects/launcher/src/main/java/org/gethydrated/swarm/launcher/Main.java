package org.gethydrated.swarm.launcher;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.log.JDKModuleLogger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        Module.setModuleLogger(new JDKModuleLogger(java.util.logging.Logger.getLogger("org.jboss.modules"),
                java.util.logging.Logger.getLogger("org.jboss.modules.define")) );
        if (args.length == 1 && args[0].equals("-h")) {
        	printUsage(System.out);
        	SystemExit.exit(ExitCode.EXIT);
        }
        try {
            Module.registerURLStreamHandlerFactoryModule(Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.jboss.jboss-vfs")));
            validateSwarmDirectories();
            configureLogback();
            Config cfg = AkkaConfigBuilder.build(args);
            if (cfg != null) {
	            final ActorSystem system = ActorSystem.create("swarm", cfg);
	            system.actorOf(Props.create(Reaper.class), "reaper");
	            
	            Thread t = new Thread(new Runnable() {
	
					@Override
					public void run() {
						BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
						while(true) {
							try {
								String inp = buffer.readLine();
								handleInput(inp, system);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
					}});
	            t.setDaemon(true);
	            t.start();
            }
        } catch (Throwable t) {
            fail(t);
        }
    }

    private static void configureLogback() throws MalformedURLException {
    	final Map<String, String> properties = new HashMap<>();
        properties.put("HYDRA_HOME", System.getProperty("swarm.home.dir"));
        VirtualFile homeDir = VFS.getChild(System.getProperty("swarm.home.dir"));
        VirtualFile confDir = (System.getProperty("swarm.conf.dir") == null) ? homeDir.getChild("conf") : VFS.getChild(System.getProperty("swarm.conf.dir"));
        VirtualFile logbackCfg = confDir.getChild("logging.xml");
        if (logbackCfg.exists()) {
        	LogbackConfigurator.configure(logbackCfg.asFileURL(), properties);
        } else {
        	throw new IllegalStateException("Could not find logging.xml at " + confDir + ".");
        }
	}

	public static void fail(Throwable t) {
        if (t != null) {
            t.printStackTrace(System.err);
        }
        SystemExit.exit(ExitCode.FAIL);
    }
    
    
    public static void printUsage(PrintStream out) {
		out.println("Usage:");
		out.println(" swarm <options>");
		out.println("  where possible options include:");
		out.println("   -h          This usage information");
		out.println("   -p <int>    Port used by swarm clustering layer");
		out.println("   -p          Ask for port on startup");
		out.println("   -hn         Hostname used by swarm clustering layer");
		out.println("   -r <id>     Adds a new role to this swarm instance");	
	}
    
	protected static void handleInput(String inp, ActorSystem system) {
    	switch(inp) {
    		case "exit":
    		case "x":
    			system.shutdown();
    			system.awaitTermination();
    			break;
    		default:
    			break;
    	}
	}
    
	private static void validateSwarmDirectories() throws IOException, URISyntaxException {
        VirtualFile homeDir = VFS.getChild(System.getProperty("swarm.home.dir"));
        checkDirectory(homeDir);
        VirtualFile confDir = (System.getProperty("swarm.conf.dir") == null) ? homeDir.getChild("conf") : VFS.getChild(System.getProperty("swarm.conf.dir"));
        checkDirectory(confDir);
        VirtualFile logDir = (System.getProperty("swarm.log.dir") == null) ? homeDir.getChild("log") : VFS.getChild(System.getProperty("swarm.log.dir"));
        checkDirectory(logDir);
        checkDirectory(homeDir.getChild("deploy"));
        System.setProperty("swarm.deploy.dir", homeDir.getChild("deploy").getPhysicalFile().toString());
        checkDirectory(homeDir.getChild("webapps"));
        System.setProperty("swarm.webapp.dir", homeDir.getChild("webapps").getPhysicalFile().toString());
    }

    private static void checkDirectory(VirtualFile dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Invalid directory: '" + dir.getPathName() + "'.");
        }
    }
}
