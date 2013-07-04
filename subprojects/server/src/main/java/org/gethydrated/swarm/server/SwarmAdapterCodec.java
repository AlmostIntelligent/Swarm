package org.gethydrated.swarm.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 *
 */
public class SwarmAdapterCodec extends MessageToMessageCodec<FullHttpRequest, HttpResponse> {

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, MessageList<Object> out) throws Exception {
        SwarmHttpRequest request = new SwarmHttpRequest();
        request.setMethod(msg.getMethod().name())
                .setUri(msg.getUri())
                .setHttpVersion(msg.getProtocolVersion().text())
                .addHeaders(msg.headers().entries());
        out.add(request);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpResponse msg, MessageList<Object> out) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.valueOf(msg.getHttpVersion()),
                HttpResponseStatus.valueOf(msg.getStatus()), Unpooled.copiedBuffer(msg.getContentBuffer(), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, msg.getContentType());
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        if (msg.isKeepAlive()) {
            System.out.println("keep alive");
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
        }
        out.add(response);
    }

}
