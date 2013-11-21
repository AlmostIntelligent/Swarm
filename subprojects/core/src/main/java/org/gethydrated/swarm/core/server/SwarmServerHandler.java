package org.gethydrated.swarm.core.server;

import org.gethydrated.swarm.core.messages.http.SwarmHttpRequest;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;

@Sharable
public class SwarmServerHandler extends SimpleChannelInboundHandler<SwarmHttpRequest> {

    private ActorSelection router;
    private ActorRef self;
    private ChannelMapper channelMapper;

    public SwarmServerHandler(ActorSelection router, ChannelMapper channelMapper, ActorRef self) {
        this.router = router;
        this.channelMapper = channelMapper;
        this.self = self;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SwarmHttpRequest msg) throws Exception {
        long id = channelMapper.addChannel(ctx);
        msg.setRequestId(id);
        router.tell(msg, self);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
