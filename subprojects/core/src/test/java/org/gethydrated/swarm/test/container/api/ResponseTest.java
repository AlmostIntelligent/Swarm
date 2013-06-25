package org.gethydrated.swarm.test.container.api;

import org.gethydrated.swarm.server.Response;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class ResponseTest {

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        Response req = new Response();

        req.getWriter().write("hello world!");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(req);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Response deser = (Response) ois.readObject();

        assertEquals(deser.getContent(), req.getContent());
        assertFalse(deser == req);
    }
}
