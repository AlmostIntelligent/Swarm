package org.gethydrated.swarm.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.gethydrated.swarm.mapping.MappingService;

/**
 *
 */
@Sharable
public class SwarmServerHandler extends SimpleChannelInboundHandler<SwarmHttpRequest> {

    private MappingService mappingService;
    private ChannelMapper channelMapper;

    public SwarmServerHandler(MappingService mappingService, ChannelMapper channelMapper) {
        this.mappingService = mappingService;
        this.channelMapper = channelMapper;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, SwarmHttpRequest msg) throws Exception {
        long id = channelMapper.addChannel(ctx);
        msg.setRequestId(id);
        mappingService.map(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
