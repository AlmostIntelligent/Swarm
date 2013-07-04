package org.gethydrated.swarm.mapping;

import org.gethydrated.swarm.server.ServerService;
import org.gethydrated.swarm.server.SwarmHttpRequest;
import org.gethydrated.swarm.server.SwarmHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MappingService {

    private Logger logger = LoggerFactory.getLogger(MappingService.class);

    private Map<String, Invokable> mappings = new HashMap<>();

    private Invokable defaultWebApp;

    private ServerService serverService;

    public void start() throws Exception {
    }

    public void stop() throws Exception {
        for (Invokable i : mappings.values()) {
            i.stop();
        }
        if (defaultWebApp != null) {
            defaultWebApp.stop();
        }
    }

    public void addApplication(String contextPath, Invokable webapp) {
        if (contextPath.equals("")) {
            if (defaultWebApp != null) {
                throw new IllegalStateException("Default web application already set.");
            }
            defaultWebApp = webapp;
        } else {
            this.mappings.put(contextPath, webapp);
        }
    }

    public void map(SwarmHttpRequest request) throws ServletException, IOException {
        if (defaultWebApp != null) {
            defaultWebApp.invoke(request);
        } else {
            SwarmHttpResponse response = new SwarmHttpResponse();
            response.setRequestId(request.getRequestId());
            response.setHttpVersion(request.getHttpVersion());
            response.setStatus(404);
            serverService.send(response);
        }
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }
}
