package org.gethydrated.swarm.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.MessageList;
import io.netty.util.CharsetUtil;

import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class SwarmServerHandler extends ChannelInboundHandlerAdapter {

    private int calls;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageList<Object> msgs) throws Exception {
        MessageList<Object> out = MessageList.newInstance();
        int size = msgs.size();
        for (int i = 0; i < size; i++) {
            StringBuilder buf = new StringBuilder();
            buf.append("Hello World ");
            buf.append(calls);
            ByteBuf res = Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8);
            Object msg = msgs.get(i);
            if (msg instanceof Request) {
                Request req = (Request) msg;
                HttpServletResponse response = new Response();
                response.setContentType("text/plain");
                response.setContentLength(res.readableBytes());
                response.getWriter().write(buf.toString());
                out.add(response);
            }
        }
        ctx.write(out);
        msgs.releaseAllAndRecycle();
        calls++;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
