package org.gethydrated.swarm.core.server;

import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;

public class HttpServer extends UntypedActor {

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ServerBootstrap serverBootstrap;
	private ActorSelection router;
	private ChannelMapper channelMapper;
	
	@Override
	public void onReceive(Object obj) throws Exception {
		if(obj instanceof SwarmHttpResponse) {
			send((SwarmHttpResponse) obj);
		} else {
			unhandled(obj);
		}
	}

	@Override
	public void preStart() throws InterruptedException {
		channelMapper = new ChannelMapper(context().system());
		router = context().actorSelection("/user/http-request-router");
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024)
        	.group(bossGroup, workerGroup)
        	.channel(NioServerSocketChannel.class)
        	.childHandler(new SwarmServerInitializer(router, channelMapper, self()));
		serverBootstrap.bind(context().system().settings().config().getInt("http.port")).sync().channel();
		
	}
	
	@Override
	public void postStop() {
		if (channelMapper != null) {
			channelMapper.destroy();
		}
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
	
	private void send(SwarmHttpResponse response) {
		ChannelHandlerContext ctx = channelMapper.removeChannel(response.getRequestId());
		if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(response).syncUninterruptibly();
        }
	}
}
