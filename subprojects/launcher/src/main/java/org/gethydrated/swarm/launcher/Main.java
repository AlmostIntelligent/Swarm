package org.gethydrated.swarm.launcher;

import org.gethydrated.swarm.mapping.MappingService;
import org.gethydrated.swarm.scanner.ScannerService;
import org.gethydrated.swarm.server.ServerService;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.log.JDKModuleLogger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import java.io.IOException;

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

            MappingService mapper = new MappingService();
            ServerService server = new ServerService();
            ScannerService scanner = new ScannerService();

            mapper.setServerService(server);
            server.setMappingService(mapper);
            scanner.setMappingService(mapper);
            scanner.setServerService(server);

            server.start();
            scanner.start();

            System.in.read();

            scanner.stop();
            mapper.stop();
            server.stop();
        } catch (Throwable t) {
            fail(t);
        }
    }

    private static void validateSwarmDirectories() throws IOException {
        VirtualFile homeDir = VFS.getChild(System.getProperty("swarm.home.dir"));
        checkDirectory(homeDir);
        VirtualFile confDir = (System.getProperty("swarm.conf.dir") == null) ? homeDir.getChild("conf") : VFS.getChild(System.getProperty("swarm.conf.dir"));
        checkDirectory(confDir);
        VirtualFile logDir = (System.getProperty("swarm.log.dir") == null) ? homeDir.getChild("log") : VFS.getChild(System.getProperty("swarm.log.dir"));
        checkDirectory(logDir);
        checkDirectory(homeDir.getChild("deploy"));
        checkDirectory(homeDir.getChild("webapps"));
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
