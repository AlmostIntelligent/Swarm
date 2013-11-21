package org.gethydrated.swarm.core.deploy;

import org.gethydrated.swarm.core.messages.deploy.DeploymentDescriptor;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 *
 */
public class DeploymentDescriptorFactory {
    public static DeploymentDescriptor create(VirtualFile handle) throws Exception {
        VirtualFile defaultWebXml = VFS.getChild(System.getProperty("swarm.conf.dir")).getChild("default-web.xml");
        DeploymentDescriptor descriptor = new DeploymentDescriptor();
        descriptor = new DeploymentDescriptorReader(descriptor).parse(defaultWebXml.openStream());
        VirtualFile bundledWebXml = handle.getChild("WEB-INF/web.xml");
        if (bundledWebXml.exists()) {
            descriptor = new DeploymentDescriptorReader(descriptor).parse(bundledWebXml.openStream());
        }
        return descriptor;
    }
}
