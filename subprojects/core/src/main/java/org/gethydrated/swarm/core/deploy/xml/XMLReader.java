package org.gethydrated.swarm.core.deploy.xml;

import java.io.InputStream;

/**
 * Generic XML reader.
 * 
 * @param <T> Result type.
 * @author Christian Kulpa
 * @author Hanno Sternberg
 * @since 0.2.0
 */
public interface XMLReader<T> {

    /**
     * Parses an XML document.
     * @param inputStream XML input.
     * @return Parse result.
     */
    T parse(InputStream inputStream) throws Exception;
}
