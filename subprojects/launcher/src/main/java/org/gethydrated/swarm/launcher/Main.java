package org.gethydrated.swarm.launcher;

import org.gethydrated.hydra.api.service.SID;
import org.gethydrated.hydra.core.HydraFactory;
import org.gethydrated.hydra.core.InternalHydra;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.log.JDKModuleLogger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        Module.setModuleLogger(new JDKModuleLogger(java.util.logging.Logger.getLogger("org.jboss.modules"),
                java.util.logging.Logger.getLogger("org.jboss.modules.define")) );


        try {
            Module.registerURLStreamHandlerFactoryModule(Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.jboss.jboss-vfs")));
            validateSwarmDirectories();

            HydraFactory factory = HydraFactory.create();
            factory.addResolver(new SwarmServiceResolver());
            InternalHydra hydra = (InternalHydra) factory.getHydra();
            SID mapping = hydra.startService("swarm::mapping");
            //WORKAROUND: sync with registration. Needs rewrite in hydra.
            mapping.ask("init").get();
            hydra.startService("swarm::server");
            hydra.startService("swarm::scanner");
            hydra.await();
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
