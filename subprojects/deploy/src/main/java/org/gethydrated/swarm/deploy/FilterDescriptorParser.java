package org.gethydrated.swarm.deploy;

import org.gethydrated.swarm.deploy.xml.XMLParser;
import org.w3c.dom.Element;

/**
 *
 */
public class FilterDescriptorParser implements XMLParser<FilterDescriptor> {
    private boolean complete = false;

    private FilterDescriptor filter;

    private String paramName;

    private String paramValue;

    @Override
    public FilterDescriptor getResult() {
        return complete ? filter : null;
    }

    @Override
    public void startElement(Element element) {
        switch (element.getNodeName()) {
            case "filter": startServlet(); break;
            case "filter-name": servletName(element); break;
            case "filter-class": servletClass(element); break;
            case "param-name": paramName(element); break;
            case "param-value": paramValue(element); break;
        }
    }

    private void paramValue(Element element) {
        if (paramName != null) {
            filter.addInitParameter(paramName, element.getTextContent().replaceAll("\\s", ""));
            paramName = null;
        } else {
            paramValue = element.getTextContent().replaceAll("\\s", "");
        }

    }

    private void paramName(Element element) {
        if (paramValue != null) {
            filter.addInitParameter(element.getTextContent().replaceAll("\\s", ""), paramValue);
        } else {
            paramName = element.getTextContent().replaceAll("\\s", "");
        }
    }

    @Override
    public void endElement(Element element) {
        if (element.getNodeName().equals("filter")) {
            complete = true;
        }
    }

    private void servletClass(Element element) {
        filter.setClassName(element.getTextContent().replaceAll("\\s", ""));
    }

    private void servletName(Element element) {
        filter.setName(element.getTextContent().replaceAll("\\s", ""));
    }

    private void startServlet() {
        filter = new FilterDescriptor();
    }
}
