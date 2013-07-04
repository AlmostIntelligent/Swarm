package org.gethydrated.swarm.scanner;

import org.gethydrated.swarm.container.WebAppService;
import org.gethydrated.swarm.mapping.MappingService;
import org.gethydrated.swarm.server.ServerService;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import java.util.List;

/**
 *
 */
public class ScannerService {

    private ServerService serverService;
    private MappingService mappingService;

    public void init() throws Exception {

    }

    public void start() throws Exception {
        VirtualFile webappsDir = VFS.getChild(System.getProperty("swarm.home.dir") + "/webapps");
        List<VirtualFile> webapps = webappsDir.getChildren(new VirtualFileFilter() {
            @Override
            public boolean accepts(VirtualFile file) {
                return (file.exists() && (file.isDirectory() || file.getName().endsWith(".war")));
            }
        });
        for (VirtualFile file : webapps) {
            WebAppService service = new WebAppService(file);
            service.setServer(serverService);
            service.init();
            service.start();
            mappingService.addApplication(service.getApplication().getContextPath(), service);
        }
    }

    public void stop() throws Exception {
    }

    public void destroy() throws  Exception {

    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    public ServerService getServerService() {
        return serverService;
    }

    public void setMappingService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public MappingService getMappingService() {
        return mappingService;
    }
}
