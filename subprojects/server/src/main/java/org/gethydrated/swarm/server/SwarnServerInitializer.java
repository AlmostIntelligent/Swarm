package org.gethydrated.swarm.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.gethydrated.swarm.mapping.MappingService;

/**
 *
 */
public class SwarnServerInitializer extends ChannelInitializer<SocketChannel> {

    private SwarmServerHandler handler;

    public SwarnServerInitializer(MappingService mappingService, ChannelMapper channelMapper) {
        this.handler = new SwarmServerHandler(mappingService, channelMapper);
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast("http-codec", new HttpServerCodec());
        p.addLast("http-aggregator", new HttpObjectAggregator(65536));
        p.addLast("swarm-jee-adapter", new SwarmAdapterCodec());
        p.addLast("swarm-servlet", handler);
    }
}