package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.deploy.DeploymentDescriptor;
import org.gethydrated.swarm.deploy.DeploymentDescriptorFactory;
import org.gethydrated.swarm.deploy.FilterDescriptor;
import org.gethydrated.swarm.deploy.ServletDescriptor;
import org.gethydrated.swarm.modules.DeploymentModuleLoader;
import org.gethydrated.swarm.modules.VFSResourceLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.FilterVirtualFileVisitor;
import org.jboss.vfs.util.SuffixMatchFilter;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

/**
 *
 */
public class ApplicationContextFactory {

    public static ApplicationContext create(VirtualFile handle) throws Exception {
        DeploymentDescriptor descriptor = DeploymentDescriptorFactory.create(handle);
        ApplicationContext ctx = new ApplicationContext(handle.getName());
        for (ServletDescriptor d : descriptor.getServlets()) {
            Dynamic reg = ctx.addServlet(d.getName(), d.getClassName());
            reg.setLoadOnStartup(d.getLoadOnStartup());
            for (Entry<String, String> e : d.getInitParameters().entrySet()) {
                reg.setInitParameter(e.getKey(), e.getValue());
            }
            reg.addMapping(descriptor.getServletMapping(d.getName()).getPatterns());
        }
        for (FilterDescriptor d : descriptor.getFilters()) {
            FilterRegistration.Dynamic reg = ctx.addFilter(d.getName(), d.getClassName());
            for (Entry<String, String> e : d.getInitParameters().entrySet()) {
                reg.setInitParameter(e.getKey(), e.getValue());
            }
            reg.addMappingForUrlPatterns(null, true, descriptor.getFilterMapping(d.getName()).getPatterns());
        }
        DeploymentModuleLoader ml = new DeploymentModuleLoader(Module.getBootModuleLoader());
        ml.addResourceLoader(new VFSResourceLoader(handle, handle.getName()));
        addResources(handle, ml, ctx);
        ctx.setModuleLoader(ml);
        Module m = ml.loadModule(ModuleIdentifier.fromString("deploy."+ctx.getName()));
        ml.relinkModule(m);
        System.out.println("path: " + m.getExportedPaths());
        ctx.setRoot(handle);
        ctx.setAttribute("org.apache.catalina.jsp_classpath", "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\javax\\servlet\\servlet-api\\main\\javax.servlet-api-3.1.0.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\javax\\servlet\\jsp\\servletjsp-api\\main\\javax.servlet.jsp-api-2.3.1.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\org\\glassfish\\web\\servletjsp\\main\\javax.servlet.jsp-2.3.2.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\org\\glassfish\\el\\main\\javax.el-3.0.0.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\webapps\\examples\\WEB-INF\\classes;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\webapps\\examples\\WEB-INF\\lib\\standard.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\webapps\\examples\\WEB-INF\\lib\\jstl.jar");

        ctx.addJspTagLib("http://tomcat.apache.org/debug-taglib", "/WEB-INF/jsp/debug-taglib.tld");
        ctx.addJspTagLib("http://tomcat.apache.org/example-taglib", "/WEB-INF/jsp/example-taglib.tld");
        ctx.addJspTagLib("http://tomcat.apache.org/jsp2-example-taglib", "/WEB-INF/jsp2/jsp2-example-taglib.tld");
        return ctx;
    }

    private static void addResources(VirtualFile root, DeploymentModuleLoader dml, ApplicationContext ctx) throws IOException {
        if (root.getChild("WEB-INF/classes").exists()) {
            dml.addResourceLoader(new VFSResourceLoader(root.getChild("WEB-INF/classes"), "WEB-INF/classes"));
        }
        if (root.getChild("WEB-INF/lib").exists()) {
            TempFileProvider tmp = TempFileProvider.create("tmp", Executors.newScheduledThreadPool(2));
            SuffixMatchFilter jarFilter = new SuffixMatchFilter(".jar");
            FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(jarFilter, VisitorAttributes.RECURSE);
            root.getChild("WEB-INF/lib").visit(visitor);
            for (VirtualFile file : visitor.getMatched()) {
                VFS.mountZip(file, file, tmp);
                dml.addResourceLoader(new VFSResourceLoader(file, file.getName()));
            }
        }

    }
}
