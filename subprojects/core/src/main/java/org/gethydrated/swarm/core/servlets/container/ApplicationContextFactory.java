package org.gethydrated.swarm.core.servlets.container;

import org.gethydrated.swarm.core.deploy.DeploymentDescriptorFactory;
import org.gethydrated.swarm.core.messages.deploy.DeploymentDescriptor;
import org.gethydrated.swarm.core.messages.deploy.FilterDescriptor;
import org.gethydrated.swarm.core.messages.deploy.ServletDescriptor;
import org.gethydrated.swarm.core.servlets.modules.DeploymentModuleLoader;
import org.gethydrated.swarm.core.servlets.modules.VFSResourceLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.Resource;
import org.jboss.modules.filter.PathFilters;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.FilterVirtualFileVisitor;
import org.jboss.vfs.util.SuffixMatchFilter;

import akka.event.LoggingAdapter;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration.Dynamic;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 *
 */
public class ApplicationContextFactory {

    public static ApplicationContext create(String ctxName, VirtualFile handle, LoggingAdapter logger) throws Exception {
        DeploymentDescriptor descriptor = DeploymentDescriptorFactory.create(handle);
        ApplicationContext ctx = new ApplicationContext(ctxName);
        ctx.setLogger(logger);
        ctx.setRoot(handle);
        
        StringBuffer jspClasspath = new StringBuffer();
        DeploymentModuleLoader ml = new DeploymentModuleLoader(Module.getBootModuleLoader());
        ml.addResourceLoader(new VFSResourceLoader(handle, handle.getName()));
        addResources(handle, ml, ctx, jspClasspath);
        ctx.setModuleLoader(ml);
        Module m = ml.loadModule(ModuleIdentifier.fromString("deploy."+ctx.getName()));
        ml.relinkModule(m);
        
        addServletDescriptions(ctx, descriptor);
        
        Enumeration<URL> ite = m.getClassLoader().getResources("META-INF/spring.handlers");
        while(ite.hasMoreElements()) {
        	System.out.println(ite.nextElement());
        }
        
        for(String s : descriptor.getListeners()) {
        	@SuppressWarnings("unchecked")
			Class<EventListener> cl = (Class<EventListener>) m.getClassLoader().loadClass(s);
        	EventListener ins = cl.newInstance();
        	ctx.addListener(ins);
        }
        
        addContainerServlets(ctx);
        for (FilterDescriptor d : descriptor.getFilters()) {
            FilterRegistration.Dynamic reg = ctx.addFilter(d.getName(), d.getClassName());
            for (Entry<String, String> e : d.getInitParameters().entrySet()) {
                reg.setInitParameter(e.getKey(), e.getValue());
            }
            reg.addMappingForUrlPatterns(null, true, descriptor.getFilterMapping(d.getName()).getPatterns());
        }

        
        ServiceLoader<ServletContainerInitializer> loader = m.loadService(ServletContainerInitializer.class);
        Iterator<ServletContainerInitializer> iter = loader.iterator();
        while(iter.hasNext()) {
        	ServletContainerInitializer init = iter.next();
        	logger.debug("ServletContainerInitializer: {}", init.getClass().getCanonicalName());
        	init.onStartup(null, ctx);
        }
        
        Module spec = ml.loadModule(ModuleIdentifier.fromString("org.gethydrated.swarm.spec"));
        Set<String> files = new HashSet<>();
        Iterator<Resource> iters = spec.iterateResources(PathFilters.acceptAll());
        while(iters.hasNext()) {
        	Resource r = iters.next();
        	final JarURLConnection connection =
        	        (JarURLConnection) r.getURL().openConnection();
        	files.add(connection.getJarFileURL().getFile());
        }
        for(String s : files) {
        	jspClasspath.append(s).append(File.pathSeparatorChar);
        }
        
        ctx.setAttribute("org.apache.catalina.jsp_classpath", jspClasspath.toString());

        //ctx.addJspTagLib("http://tomcat.apache.org/debug-taglib", "/WEB-INF/jsp/debug-taglib.tld");
        //ctx.addJspTagLib("http://tomcat.apache.org/example-taglib", "/WEB-INF/jsp/example-taglib.tld");
        //ctx.addJspTagLib("http://tomcat.apache.org/jsp2-example-taglib", "/WEB-INF/jsp2/jsp2-example-taglib.tld");
        System.out.println(jspClasspath.toString());
        return ctx;
    }



	private static void addServletDescriptions(ApplicationContext ctx, DeploymentDescriptor descriptor) {
    	List<ServletDescriptor> servlets = descriptor.getServlets();
    	Collections.sort(servlets);
    	for (ServletDescriptor d : servlets) {
    		Dynamic reg = ctx.addServlet(d.getName(), d.getClassName());
            reg.setLoadOnStartup(d.getLoadOnStartup());
            for (Entry<String, String> e : d.getInitParameters().entrySet()) {
                reg.setInitParameter(e.getKey(), e.getValue());
            }
            if(!reg.addMapping(descriptor.getServletMapping(d.getName()).getPatterns()).isEmpty()) {
            	throw new RuntimeException("Servlet Mappings are not unique. Servlet: " + d.getName());
            }
    	}
	}

	private static void addContainerServlets(ApplicationContext ctx) {
		if (!ctx.hasServletMapping("/")) {
			Dynamic req = ctx.addServlet("default", "org.gethydrated.swarm.servlets.DefaultServlet");
			req.addMapping("/", req.getName());
		}
		if (!ctx.hasServletMapping("*.jsp") && !ctx.hasServletMapping("*.jspx")) {
			//Dynamic req = ctx.addServlet("jsp", "org.gethydrated.swarm.servlets.JSPServlet");
			Dynamic req = ctx.addServlet("jsp", "org.apache.jasper.servlet.JspServlet");
			req.setInitParameter("fork", "false");
			req.setInitParameter("xpoweredBy", "false");
			req.setInitParameter("keepGenerated", "true");
			req.setInitParameter("usePrecompiled", "false");
			req.addMapping("*.jsp", req.getName());
			req.addMapping("*.jspx", req.getName());
		}
	}
	
	private static void addResources(VirtualFile root, DeploymentModuleLoader dml, ApplicationContext ctx, StringBuffer jspClasspath) throws IOException {
        if (root.getChild("WEB-INF/classes").exists()) {
            dml.addResourceLoader(new VFSResourceLoader(root.getChild("WEB-INF/classes"), "WEB-INF/classes"));
            jspClasspath.append(root.getChild("WEB-INF/classes").getPhysicalFile()).append(File.pathSeparatorChar);
        }
        if (root.getChild("WEB-INF/lib").exists()) {
            TempFileProvider tmp = TempFileProvider.create("tmp", Executors.newScheduledThreadPool(2));
            SuffixMatchFilter jarFilter = new SuffixMatchFilter(".jar");
            FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(jarFilter, VisitorAttributes.RECURSE);
            root.getChild("WEB-INF/lib").visit(visitor);
            for (VirtualFile file : visitor.getMatched()) {
            	jspClasspath.append(file.getPhysicalFile().toString()).append(File.pathSeparatorChar);
                VFS.mountZip(file, file, tmp);
                dml.addResourceLoader(new VFSResourceLoader(file, file.getName()));
            }
        }

    }
}