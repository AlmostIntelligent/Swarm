package org.gethydrated.swarm.deploy;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import java.io.IOException;

/**
 *
 */
public class DeploymentDescriptorFactory {
    public static DeploymentDescriptor create(VirtualFile handle) {
        VirtualFile defaultWebXml = VFS.getChild(System.getProperty("swarm.conf.dir")).getChild("default-web.xml");
        DeploymentDescriptor descriptor = new DeploymentDescriptor();
        try {
            descriptor = new DeploymentDescriptorReader(descriptor).parse(defaultWebXml.openStream());
            VirtualFile bundledWebXml = handle.getChild("WEB-INF/web.xml");
            if (bundledWebXml.exists()) {
                descriptor = new DeploymentDescriptorReader(descriptor).parse(bundledWebXml.openStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return descriptor;
    }
}
