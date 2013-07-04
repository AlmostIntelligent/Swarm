package org.gethydrated.swarm.deploy;

import org.gethydrated.swarm.deploy.xml.XMLParser;
import org.w3c.dom.Element;

/**
 *
 */
public class DeploymentDescriptorParser implements XMLParser<DeploymentDescriptor> {

    private DeploymentDescriptor deploymentDescriptor;

    private XMLParser<?> delegate;

    public DeploymentDescriptorParser(DeploymentDescriptor deploymentDescriptor) {
        this.deploymentDescriptor = deploymentDescriptor;
    }

    @Override
    public DeploymentDescriptor getResult() {
        return deploymentDescriptor;
    }

    @Override
    public void startElement(Element element) {
        switch (element.getNodeName()) {
            case "servlet": startServlet(element); break;
            case "filter": startFilter(element); break;
            case "servlet-mapping": startServletMapping(element); break;
            case "filter-mapping": startFilterMapping(element); break;
            default:
                if (delegate != null) {
                    delegate.startElement(element);
                }
        }
    }

    @Override
    public void endElement(Element element) {
        switch (element.getNodeName()) {
            case "servlet": endServlet(element); break;
            case "filter": endFilter(element); break;
            case "servlet-mapping": endServletMapping(element); break;
            case "filter-mapping": endFilterMapping(element); break;
            default:
                if (delegate != null) {
                    delegate.endElement(element);
                }
        }
    }

    private void startFilter(Element element) {
        delegate = new FilterDescriptorParser();
        delegate.startElement(element);
    }

    private void startServlet(Element element) {
        delegate = new ServletDescriptorParser();
        delegate.startElement(element);
    }

    private void startServletMapping(Element element) {
        delegate = new ServletMappingParser();
        delegate.startElement(element);
    }

    private void startFilterMapping(Element element) {
        delegate = new FilterMappingParser();
        delegate.startElement(element);
    }

    private void endFilter(Element element) {
        delegate.endElement(element);
        deploymentDescriptor.addFilter((FilterDescriptor) delegate.getResult());
        delegate = null;
    }

    private void endServlet(Element element) {
        delegate.endElement(element);
        deploymentDescriptor.addServlet((ServletDescriptor) delegate.getResult());
        delegate = null;
    }


    private void endServletMapping(Element element) {
        delegate.endElement(element);
        deploymentDescriptor.addServletMapping((Mapping) delegate.getResult());
        delegate = null;

    }

    private void endFilterMapping(Element element) {
        delegate.endElement(element);
        deploymentDescriptor.addFilterMapping((Mapping) delegate.getResult());
        delegate = null;
    }
}
