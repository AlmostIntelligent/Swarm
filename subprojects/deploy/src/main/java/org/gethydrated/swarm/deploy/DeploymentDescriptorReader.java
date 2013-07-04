package org.gethydrated.swarm.deploy;

import org.gethydrated.swarm.deploy.xml.AbstractXMLReader;
import org.gethydrated.swarm.deploy.xml.XMLParser;

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
