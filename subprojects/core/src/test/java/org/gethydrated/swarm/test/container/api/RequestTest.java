package org.gethydrated.swarm.test.container.api;

import org.gethydrated.swarm.server.Request;
import org.junit.Test;

import java.io.*;

/**
 *
 */
public class RequestTest {

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        Request req = new Request();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(req);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        req = (Request) ois.readObject();
    }
}
