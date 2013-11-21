package org.gethydrated.swarm.core.deploy;

import org.gethydrated.swarm.core.deploy.xml.AbstractXMLReader;
import org.gethydrated.swarm.core.deploy.xml.XMLParser;
import org.gethydrated.swarm.core.messages.deploy.DeploymentDescriptor;

/**
 *
 */
public class DeploymentDescriptorReader extends AbstractXMLReader<DeploymentDescriptor> {

    private DeploymentDescriptor deploymentDescriptor;

    public DeploymentDescriptorReader() {
        this(new DeploymentDescriptor());
    }

    public DeploymentDescriptorReader(DeploymentDescriptor deploymentDescriptor) {
        this.deploymentDescriptor = deploymentDescriptor;
    }

    @Override
    protected XMLParser<DeploymentDescriptor> getParser() {
        return new DeploymentDescriptorParser(deploymentDescriptor);
    }

}
