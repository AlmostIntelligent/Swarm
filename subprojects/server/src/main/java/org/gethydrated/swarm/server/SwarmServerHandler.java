package org.gethydrated.swarm.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.gethydrated.hydra.api.service.SID;

/**
 *
 */
@Sharable
public class SwarmServerHandler extends SimpleChannelInboundHandler<SwarmHttpRequest> {

    private SID mappingService;
    private SID self;
    private ChannelMapper channelMapper;

    public SwarmServerHandler(SID mappingService, ChannelMapper channelMapper, SID self) {
        this.mappingService = mappingService;
        this.channelMapper = channelMapper;
        this.self = self;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SwarmHttpRequest msg) throws Exception {
        long id = channelMapper.addChannel(ctx);
        msg.setRequestId(id);
        mappingService.tell(msg, self);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
