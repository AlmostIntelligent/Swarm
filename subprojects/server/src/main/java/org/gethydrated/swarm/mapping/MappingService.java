package org.gethydrated.swarm.mapping;

import org.gethydrated.hydra.api.HydraException;
import org.gethydrated.hydra.api.service.MessageHandler;
import org.gethydrated.hydra.api.service.SID;
import org.gethydrated.hydra.api.service.ServiceActivator;
import org.gethydrated.hydra.api.service.ServiceContext;
import org.gethydrated.swarm.messages.RegisterApp;
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
public class MappingService implements ServiceActivator {

    public static final String MAPPER_NAME = "webapp_mapper";

    private Logger logger = LoggerFactory.getLogger(MappingService.class);

    private Map<String, SID> mappings = new HashMap<>();

    private SID defaultWebApp;

    private ServiceContext context;

    public void map(SwarmHttpRequest request, SID sender) throws ServletException, IOException, HydraException {
        String uri = request.getUri();
        String idx;
        if (uri.equals("/") || !uri.substring(1).contains("/")) {
            idx = uri.substring(1);
        } else {
            idx = uri.substring(1, uri.indexOf("/", 1));
        }
        if (idx == "") {
            invokeDefault(request, sender);
            return;
        }
        SID app = mappings.get(idx);
        if (app == null) {
            invokeDefault(request, sender);
        } else {
            app.tell(request, sender);
        }
    }

    public void invokeDefault(SwarmHttpRequest request, SID sender) throws ServletException, IOException {
        if (defaultWebApp != null) {
            defaultWebApp.tell(request, sender);
        } else {
            SwarmHttpResponse response = new SwarmHttpResponse();
            response.setRequestId(request.getRequestId());
            response.setHttpVersion(request.getHttpVersion());
            response.setStatus(404);
            response.setContent("No root webapp found");
            response.setContentType("text/plain");
            sender.tell(response,sender);
        }
    }

    @Override
    public void start(final ServiceContext context) throws Exception {
        this.context = context;
        context.registerLocal(MAPPER_NAME, context.getSelf());
        context.registerMessageHandler(SwarmHttpRequest.class, new MessageHandler<SwarmHttpRequest>() {
            @Override
            public void handle(SwarmHttpRequest message, SID sender) {
                try {
                    map(message, sender);
                } catch (Exception e) {
                    logger.error("Error:", e);
                }
            }
        });
        //WORKAROUND: sync with registration
        context.registerMessageHandler(String.class, new MessageHandler<String>() {
            @Override
            public void handle(String message, SID sender) {
                if ("init".equals(message)) {
                    sender.tell("done", context.getSelf());
                }
            }
        });
        context.registerMessageHandler(RegisterApp.class, new MessageHandler<RegisterApp>() {
            @Override
            public void handle(RegisterApp message, SID sender) {
                mappings.put(message.getContextName(), sender);
            }
        });
    }

    @Override
    public void stop(ServiceContext context) throws Exception {

    }
}
