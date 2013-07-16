package org.gethydrated.swarm.mapping;

import org.gethydrated.swarm.server.ServerService;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class ScannerService extends TimerTask {

    private Timer timer = new Timer();
    private ServerService serverService;
    private MappingService mappingService;

    public void init() throws Exception {

    }

    public void start() throws Exception {
        timer.scheduleAtFixedRate(this, 0, 60000);
    }

    public void stop() throws Exception {
        timer.cancel();
    }

    public void destroy() throws  Exception {

    }

    @Override
    public void run() {
        System.out.println("scanning");
    }

    public void setServerService(org.gethydrated.swarm.server.ServerService serverService) {
        this.serverService = serverService;
    }

    public ServerService getServerService() {
        return serverService;
    }

    public void setMappingService(org.gethydrated.swarm.mapping.MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public MappingService getMappingService() {
        return mappingService;
    }
}
