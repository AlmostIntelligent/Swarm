package org.gethydrated.swarm.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 *
 */
public class SwarmAdapterCodec extends MessageToMessageCodec<FullHttpRequest, HttpResponse> {

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(false);

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, MessageList<Object> out) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(msg.getUri());
        SwarmHttpRequest request = new SwarmHttpRequest();
        request.setMethod(msg.getMethod().name())
                .setUri(decoder.path())
                .addParameters(decoder.parameters())
                .setServerName(HttpHeaders.getHost(msg))
                .setRemoteAddr((InetSocketAddress) ctx.channel().remoteAddress())
                .setLocalAddr(((InetSocketAddress) ctx.channel().localAddress()))
                .setHttpVersion(msg.getProtocolVersion().text())
                .addHeaders(msg.headers().entries());
        try {
            HttpPostRequestDecoder postdecoder = new HttpPostRequestDecoder(factory, msg);
            while (postdecoder.hasNext()) {
                InterfaceHttpData data = postdecoder.next();
                if (data.getHttpDataType() == HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    request.addParameter(attribute.getName(), attribute.getValue());
                }
            }
        } catch (EndOfDataDecoderException e) {
            //nothing to do here..
        } catch (IncompatibleDataDecoderException e) {
            //was get request, ignore
        }
        String cookieHeader = msg.headers().get("Cookie");
        if (cookieHeader != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                request.addCookie(CookieAdapter.fromNetty(cookie));
            }
        }
        out.add(request);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpResponse msg, MessageList<Object> out) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.valueOf(msg.getHttpVersion()),
                HttpResponseStatus.valueOf(msg.getStatus()), Unpooled.copiedBuffer(msg.getContentBuffer(), CharsetUtil.ISO_8859_1));

        Enumeration<String> headers = msg.getHeaderNames();
        while (headers.hasMoreElements()) {
            String s = headers.nextElement();
            Enumeration<String> h = msg.getHeaders(s);
            while (h.hasMoreElements()) {
                response.headers().add(s, h.nextElement());
            }
        }

        if (msg.getContentType() != null && !msg.getContentType().equals("")) {
            response.headers().set(CONTENT_TYPE, msg.getContentType());
        }
        if (msg.getContentLength() >= 0) {
            response.headers().set(CONTENT_LENGTH, msg.getContentLength());
        }
        for (javax.servlet.http.Cookie cookie : msg.getCookies()) {
            response.headers().add("Set-Cookie", ServerCookieEncoder.encode(CookieAdapter.toNetty(cookie)));

        }

        if (msg.isKeepAlive()) {
            System.out.println("keep alive");
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
        }
        out.add(response);
    }

}
