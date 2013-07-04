package org.gethydrated.swarm.container.core;

import org.gethydrated.swarm.deploy.DeploymentDescriptor;
import org.gethydrated.swarm.deploy.DeploymentDescriptorFactory;
import org.gethydrated.swarm.deploy.FilterDescriptor;
import org.gethydrated.swarm.deploy.ServletDescriptor;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.FilterVirtualFileVisitor;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
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
        Module m = Module.getBootModuleLoader().loadModule(ModuleIdentifier.fromString("org.gethydrated.swarm.web"));
        ClassLoader cl = createContextClassloader(handle, m.getClassLoader());
        ctx.setModule(m);
        ctx.setClassLoader(cl);
        ctx.setRoot(handle);
        return ctx;
    }

    private static ClassLoader createContextClassloader(VirtualFile handle, ClassLoader parent) throws IOException {
        if (handle == null || !handle.exists()) {
            throw new IOException("File handle not found.");
        }
        final List<URL> paths = new ArrayList<>();
        FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(new VirtualFileFilter() {
            @Override
            public boolean accepts(VirtualFile file) {
                return file.isDirectory();
            }
        }, VisitorAttributes.RECURSE);
        handle.visit(visitor);
        for (VirtualFile dir : visitor.getMatched()) {
            paths.add(dir.toURL());
        }
        return new URLClassLoader(paths.toArray(new URL[0]), parent);
    }

}
