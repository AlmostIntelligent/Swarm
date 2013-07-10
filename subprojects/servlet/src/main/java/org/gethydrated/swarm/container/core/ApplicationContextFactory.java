package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.deploy.DeploymentDescriptor;
import org.gethydrated.swarm.deploy.DeploymentDescriptorFactory;
import org.gethydrated.swarm.deploy.FilterDescriptor;
import org.gethydrated.swarm.deploy.ServletDescriptor;
import org.gethydrated.swarm.modules.DeploymentModuleLoader;
import org.gethydrated.swarm.modules.VFSResourceLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.vfs.VirtualFile;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import java.util.Map.Entry;

/**
 *
 */
public class ApplicationContextFactory {

    public static ApplicationContext create(VirtualFile handle) throws Exception {
        DeploymentDescriptor descriptor = DeploymentDescriptorFactory.create(handle);
        ApplicationContext ctx = new ApplicationContext();
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
        ml.addResourceLoader(new VFSResourceLoader(handle.getChild("WEB-INF/classes"), "WEB-INF/classes"));
        ctx.setModuleLoader(ml);
        Module m = ml.loadModule(ModuleIdentifier.fromString("deploy."+ctx.getName()));
        ml.relinkModule(m);
        ctx.setRoot(handle);
        ctx.setAttribute("org.apache.catalina.jsp_classpath", "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\javax\\servlet\\servlet-api\\main\\javax.servlet-api-3.1.0.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\javax\\servlet\\jsp\\servletjsp-api\\main\\javax.servlet.jsp-api-2.3.1.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\org\\glassfish\\web\\servletjsp\\main\\javax.servlet.jsp-2.3.2.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\modules\\system\\layers\\base\\org\\glassfish\\el\\main\\javax.el-3.0.0.jar;" +
                "D:\\git\\Swarm\\build\\swarm-1.0.0-SNAPSHOT\\webapps\\examples\\WEB-INF\\classes");

        return ctx;
    }

}
