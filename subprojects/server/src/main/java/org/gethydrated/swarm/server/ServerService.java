package org.gethydrated.swarm.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.gethydrated.hydra.api.service.MessageHandler;
import org.gethydrated.hydra.api.service.SID;
import org.gethydrated.hydra.api.service.ServiceActivator;
import org.gethydrated.hydra.api.service.ServiceContext;
import org.gethydrated.swarm.mapping.MappingService;


/**
 *
 */
public class ServerService implements ServiceActivator {

    public static final String SERVER_NAME = "swarm_server";
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    private SID mappingService;
    private ChannelMapper channelMapper;

    @Override
    public void start(ServiceContext context) throws Exception {
        mappingService = context.getLocalService(MappingService.MAPPER_NAME);
        channelMapper = new ChannelMapper();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024)
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new SwarmServerInitializer(mappingService, channelMapper, context.getSelf()));
        NioServerSocketChannel s = (NioServerSocketChannel) serverBootstrap.bind(80).sync().channel();
        context.registerMessageHandler(SwarmHttpResponse.class, new MessageHandler<SwarmHttpResponse>() {
            @Override
            public void handle(SwarmHttpResponse message, SID sender) {
                send(message);
            }
        });
    }

    @Override
    public void stop(ServiceContext context) throws Exception {
        if (channelMapper != null) {
            channelMapper.destroy();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void send(SwarmHttpResponse response) {
        ChannelHandlerContext ctx = channelMapper.removeChannel(response.getRequestId());
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(response).syncUninterruptibly();
        }
    }
}
