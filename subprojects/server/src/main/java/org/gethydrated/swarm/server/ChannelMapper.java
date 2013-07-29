package org.gethydrated.swarm.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class ChannelMapper {

    private ConcurrentHashMap<Long, ChannelHandlerContext> mappings = new ConcurrentHashMap<>();
    private AtomicLong id = new AtomicLong(0);
    private HashedWheelTimer timer = new HashedWheelTimer();

    public long addChannel(ChannelHandlerContext ctx) {
        long i = id.getAndIncrement();
        while(mappings.putIfAbsent(i, ctx) != null) {
            i = id.getAndIncrement();
        }
        timer.newTimeout(new ChannelTimeout(i), 10, TimeUnit.SECONDS);
        return i;
    }

    public ChannelHandlerContext removeChannel(long i) {
        return mappings.remove(i);
    }

    public void destroy() {
        timer.stop();
        for (ChannelHandlerContext ctx : mappings.values()) {
            ctx.close();
        }
    }

    private class ChannelTimeout implements TimerTask {

        private long id;

        public ChannelTimeout(long i) {
            this.id = i;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            ChannelHandlerContext ctx = mappings.remove(id);
            if (ctx != null) {
                LoggerFactory.getLogger(ChannelTimeout.class).info("channel timeout: id '{}'", id);
                if(ctx.channel().isActive()) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("Request timed out.");
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.GATEWAY_TIMEOUT, Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

                    ctx.writeAndFlush(response).syncUninterruptibly();
                }
                ctx.close();
            }
        }
    }
}
