package org.gethydrated.swarm.launcher;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.log.JDKModuleLogger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        Module.setModuleLogger(new JDKModuleLogger(java.util.logging.Logger.getLogger("org.jboss.modules"),
                java.util.logging.Logger.getLogger("org.jboss.modules.define")) );
        ActorSystem system = null; 
        try {
            Module.registerURLStreamHandlerFactoryModule(Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.jboss.jboss-vfs")));
            validateSwarmDirectories();
            Config cfg = ConfigBuilder.build();
            system = ActorSystem.create("swarm", cfg);

            system.actorOf(Props.create(Reaper.class), "reaper");
            ActorSelection reaper = system.actorSelection("/user/reaper");
            System.out.println(reaper);
            ActorRef ref = system.actorOf(Props.create(TestActor.class), "test");
            System.out.println(ref);
            ref.tell("die", null);
        } catch (Throwable t) {
            fail(t);
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

    public static void fail(Throwable t) {
        if (t != null) {
            t.printStackTrace(System.err);
        }
        SystemExit.exit(ExitCode.FAIL);
    }
}
