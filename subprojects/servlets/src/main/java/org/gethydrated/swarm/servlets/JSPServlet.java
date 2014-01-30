package org.gethydrated.swarm.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gethydrated.swarm.core.servlets.container.ApplicationContext;
import org.jboss.vfs.VirtualFile;
import org.slf4j.LoggerFactory;

public class JSPServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3673953493618407540L;

	private org.slf4j.Logger logger = LoggerFactory.getLogger(DefaultServlet.class);
	
	public void init(ServletConfig config) throws ServletException {
		
	}
	
	public void destroy() {
		
	}
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	resp.getWriter().println("ich bin ein jsp!");
    	resp.getWriter().println(req.getContextPath());
    	resp.getWriter().println(req.getServletPath());
    	resp.getWriter().println(req.getPathInfo());
    	if (req.getServletPath() != null && (req.getServletPath().endsWith(".jsp") || req.getServletPath().endsWith(".jspx"))) {
    		logger.info(req.getRequestURI());

            VirtualFile file = ((ApplicationContext)req.getServletContext()).getResourceAsFile(req.getServletPath());

            logger.info("file: {}", file);
            resp.getWriter().println(file);
            resp.getWriter().println(file.exists());
    	}
    	resp.flushBuffer();
    }
	
}
