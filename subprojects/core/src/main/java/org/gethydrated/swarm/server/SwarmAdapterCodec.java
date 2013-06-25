package org.gethydrated.swarm.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 *
 */
public class SwarmAdapterCodec extends MessageToMessageCodec<FullHttpRequest, Response> {

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, MessageList<Object> out) throws Exception {
        System.out.println("jjuk!");
        out.add(new Request());
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Response msg, MessageList<Object> out) throws Exception {
        System.out.println("juk!");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(msg.getContent(), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        if (!msg.isKeepAlive()) {
            out.add(response);
            ctx.write(out).addListener(ChannelFutureListener.CLOSE);
            out = MessageList.newInstance();
        } else {
            out.add(response);
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
        }
    }

}
