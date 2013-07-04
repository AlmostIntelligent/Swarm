package org.gethydrated.swarm.container.connector;

import org.gethydrated.swarm.server.SwarmHttpResponse;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class ServletWriter extends Writer {

    private final SwarmHttpResponse response;

    public ServletWriter(SwarmHttpResponse response) {
        this.response = response;
    }


    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        response.getContentBuffer().append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        System.out.println("flushed");
    }

    @Override
    public void close() throws IOException {
        System.out.println("closed");
    }
}
