package org.gethydrated.swarm.servlets;

import org.gethydrated.swarm.container.core.ApplicationContext;
import org.jboss.vfs.VirtualFile;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 */
public class DefaultServlet extends HttpServlet {

    private boolean showDirectory = false;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(DefaultServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("{}", req.getContextPath());
        logger.info("{}", req.getRequestURI());
        logger.info("{}", req.getPathInfo());
        if (req.getPathInfo() != null) {
            logger.info(req.getRequestURI());

            VirtualFile file = ((ApplicationContext)req.getServletContext()).getResourceAsFile(req.getPathInfo());

            if (file.isDirectory()) {
                if (!req.getPathInfo().endsWith("/")) {
                    resp.setStatus(HttpServletResponse.SC_FOUND);
                    resp.addHeader("Location", req.getRequestURL().append("/").toString());
                } else {
                if (showDirectory) {
                    handleDirectory();
                    } else {
                        try {
                            handleWelcomeFile(file, req, resp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                if (file.exists()) {
                    resp.setContentType(req.getServletContext().getMimeType(file.getName()));
                    try {
                        sendContent(req, resp, file.openStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        String mime = req.getServletContext().getMimeType(req.getPathInfo());
        if (mime != null) {
            resp.setContentType(mime);
        }
    }

    private void handleWelcomeFile(VirtualFile file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Set<String> welcomes = ((ApplicationContext) req.getServletContext()).getWelcomeFiles();
        for (String w : welcomes) {
            VirtualFile child = file.getChild(w);
            if (child.exists()) {
                sendContent(req, resp, child.openStream());
                return;
            }
        }
    }

    private void sendContent(HttpServletRequest req, HttpServletResponse resp, InputStream is) throws IOException {
        if (is == null) {
            return;
        }
        PrintWriter w = resp.getWriter();
        int b;

        while ((b = is.read()) >= 0) {
            w.print((char)b);
        }
    }

    private void handleDirectory() {
        //TODO: show directory listing

    }
}
