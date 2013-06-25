package org.gethydrated.swarm.server;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class ServletWriter extends Writer {

    private StringBuffer buf;

    public ServletWriter(StringBuffer buf) {
        this.buf = buf;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        buf.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}
