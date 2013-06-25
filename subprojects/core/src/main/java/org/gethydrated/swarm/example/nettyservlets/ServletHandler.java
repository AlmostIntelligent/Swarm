package org.gethydrated.swarm.example.nettyservlets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.gethydrated.swarm.container.WebAppContainer;

/**
 *
 */
public class ServletHandler extends SimpleChannelInboundHandler<Object> {

    private WebAppContainer webapp;

    public ServletHandler(WebAppContainer webapp) {
        this.webapp = webapp;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg.getClass());
    }
}
