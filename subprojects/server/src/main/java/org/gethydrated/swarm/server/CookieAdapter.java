package org.gethydrated.swarm.server;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;

/**
 *
 */
public class CookieAdapter {

    public static Cookie toNetty(javax.servlet.http.Cookie source) {
        DefaultCookie result = new DefaultCookie(source.getName(), source.getValue());

        return result;
    }

    public static javax.servlet.http.Cookie fromNetty(Cookie source) {
        javax.servlet.http.Cookie result = new javax.servlet.http.Cookie(source.getName(), source.getValue());

        return result;
    }

}
