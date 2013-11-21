package org.gethydrated.swarm.core.servlets.connector;


import java.io.IOException;
import java.io.Writer;

import org.gethydrated.swarm.core.messages.http.SwarmHttpResponse;

/**
 *
 */
public class ServletWriter extends Writer {

    private final SwarmHttpResponse response;

    private StringBuffer buf = new StringBuffer();

    public ServletWriter(SwarmHttpResponse response) {
        this.response = response;
    }


    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        buf.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        response.appendContent(buf.toString());
        buf = new StringBuffer();
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}