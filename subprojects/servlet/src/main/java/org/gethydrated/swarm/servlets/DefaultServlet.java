package org.gethydrated.swarm.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 *
 */
public class DefaultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        System.out.println(req.getPathInfo());
        if (req.getPathInfo() != null) {
            InputStream is = req.getServletContext().getResourceAsStream(req.getPathInfo().substring(1, req.getPathInfo().length()));
            try {
                if (is != null) {
                    PrintWriter w = resp.getWriter();
                    int b = 0;

                    while ((b = is.read()) >= 0) {
                        w.print((char)b);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
