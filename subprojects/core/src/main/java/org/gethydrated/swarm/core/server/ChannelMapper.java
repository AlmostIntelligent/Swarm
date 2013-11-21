package org.gethydrated.swarm.core.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import scala.concurrent.duration.FiniteDuration;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.actor.Scheduler;

public class ChannelMapper {

	private ConcurrentHashMap<Long, ChannelHandlerContext> mappings = new ConcurrentHashMap<>();
	private AtomicLong id = new AtomicLong(0);
	private Scheduler scheduler;
	private volatile boolean closed = false;
	private ActorSystem system;
	private FiniteDuration timeout;
	
	public ChannelMapper(ActorSystem system) {
		this.scheduler = system.scheduler();
		this.system = system;
		Config cfg = system.settings().config();
		if (cfg.hasPath("http.request-timeout")) {
			timeout = new FiniteDuration(cfg.getMilliseconds("http.request-timeout"), TimeUnit.MILLISECONDS);
		} else {
			timeout = new FiniteDuration(5000, TimeUnit.MILLISECONDS);
		}
	}

	public long addChannel(ChannelHandlerContext ctx) {
		if (closed) {
			throw new IllegalStateException("ChannelMapper already destroyed.");
		}
		long i = id.getAndIncrement();
		while(mappings.putIfAbsent(i, ctx) != null) {
			i = id.getAndIncrement();
		}
		scheduler.scheduleOnce(timeout, new ChannelTimeout(i), system.dispatcher());
		return i;
	}
	
	public ChannelHandlerContext removeChannel(long i) {
        return mappings.remove(i);
    }
	
	public void destroy() {
        closed = true;
        for (ChannelHandlerContext ctx : mappings.values()) {
        	sendDefaultResponse(ctx);
            ctx.close();
        }
    }
	
	private static void sendDefaultResponse(ChannelHandlerContext ctx) {
		if(ctx.channel().isActive()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Request timed out.");
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.GATEWAY_TIMEOUT, Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

            ctx.writeAndFlush(response).syncUninterruptibly();
        }
	}
	
	private class ChannelTimeout implements Runnable {
		
		private long id;

        public ChannelTimeout(long i) {
            this.id = i;
        }
		
		@Override
		public void run() {
			ChannelHandlerContext ctx = mappings.remove(id);
			if (ctx != null) {
                //LoggerFactory.getLogger(ChannelTimeout.class).info("channel timeout: id '{}'", id);
				sendDefaultResponse(ctx);
                ctx.close();
            }
		}
	}
}
