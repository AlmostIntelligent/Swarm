package org.gethydrated.swarm.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.gethydrated.swarm.mapping.MappingService;


/**
 *
 */
public class ServerService {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    private volatile boolean running = false;
    private MappingService mappingService;
    private ChannelMapper channelMapper;

    public void start() throws Exception {
        if(!running) {
            channelMapper = new ChannelMapper();
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SwarnServerInitializer(mappingService, channelMapper));
            serverBootstrap.bind(80).sync().channel();
            running = true;
        }
    }

    public void stop() throws Exception {
        if (running) {
            channelMapper.destroy();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void setMappingService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public void send(SwarmHttpResponse response) {
        if (running) {
            ChannelHandlerContext ctx = channelMapper.removeChannel(response.getRequestId());
            if (ctx != null && ctx.channel().isActive()) {
                ctx.write(response).syncUninterruptibly();
            }
        }
    }
}
