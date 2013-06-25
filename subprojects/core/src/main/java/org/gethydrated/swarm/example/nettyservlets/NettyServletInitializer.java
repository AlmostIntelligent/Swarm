package org.gethydrated.swarm.example.nettyservlets;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.gethydrated.swarm.container.WebAppContainer;
import org.gethydrated.swarm.server.SwarmAdapterCodec;

/**
 *
 */
public class NettyServletInitializer extends ChannelInitializer<SocketChannel> {

    private WebAppContainer webapp;

    public NettyServletInitializer(WebAppContainer webapp) {
        this.webapp = webapp;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("codec", new HttpServerCodec());
        p.addLast("http-aggregator", new HttpObjectAggregator(65536));
        p.addLast("swarm-jee-adapter", new SwarmAdapterCodec());
        p.addLast("servlet", new ServletHandler(webapp));
    }
}
