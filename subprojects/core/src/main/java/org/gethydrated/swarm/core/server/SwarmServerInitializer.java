package org.gethydrated.swarm.core.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class SwarmServerInitializer extends ChannelInitializer<SocketChannel> {

	private SwarmServerHandler handler;
	
	public SwarmServerInitializer(ActorSelection router,
			ChannelMapper channelMapper, ActorRef self) {
		this.handler = new SwarmServerHandler(router, channelMapper, self);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		
		p.addLast("http-codec", new HttpServerCodec());
		p.addLast("http-aggregator", new HttpObjectAggregator(65636));
		p.addLast("swarm-jee-adapter", new SwarmAdapterCodec());
		p.addLast("swarm-servlet", handler);
	}

}

