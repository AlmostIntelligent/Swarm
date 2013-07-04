package org.gethydrated.swarm.launcher.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.gethydrated.swarm.container.WebAppContainer;
import org.gethydrated.swarm.server.SwarmHttpRequest;
import org.gethydrated.swarm.server.SwarmHttpResponse;

/**
 *
 */
public class ServletHandler extends SimpleChannelInboundHandler<SwarmHttpRequest> {

    private WebAppContainer webapp;

    public ServletHandler(WebAppContainer webapp) {
        this.webapp = webapp;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, SwarmHttpRequest msg) throws Exception {
        System.out.println(msg.getClass());
        SwarmHttpResponse response = new SwarmHttpResponse();
        webapp.invoke(msg, response);

    }
}
