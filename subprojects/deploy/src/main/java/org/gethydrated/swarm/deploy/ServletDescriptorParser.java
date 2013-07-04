package org.gethydrated.swarm.deploy;

import org.gethydrated.swarm.deploy.xml.XMLParser;
import org.w3c.dom.Element;

/**
 *
 */
public class ServletDescriptorParser implements XMLParser<ServletDescriptor> {

    private boolean complete = false;

    private ServletDescriptor servlet;

    private String paramName;

    private String paramValue;

    @Override
    public ServletDescriptor getResult() {
        return complete ? servlet : null;
    }

    @Override
    public void startElement(Element element) {
        switch (element.getNodeName()) {
            case "servlet": startServlet(); break;
            case "servlet-name": servletName(element); break;
            case "servlet-class": servletClass(element); break;
            case "load-on-startup": loadOnStartup(element); break;
            case "param-value": paramValue(element); break;
            case "param-name": paramName(element); break;
        }
    }

    @Override
    public void endElement(Element element) {
        if (element.getNodeName().equals("servlet")) {
            complete = true;
        }
    }

    private void paramValue(Element element) {
        if (paramName != null) {
            servlet.addInitParameter(paramName, element.getTextContent().replaceAll("\\s", ""));
            paramName = null;
        } else {
            paramValue = element.getTextContent().replaceAll("\\s", "");
        }

    }

    private void paramName(Element element) {
        if (paramValue != null) {
            servlet.addInitParameter(element.getTextContent().replaceAll("\\s", ""), paramValue);
        } else {
            paramName = element.getTextContent().replaceAll("\\s", "");
        }
    }

    private void loadOnStartup(Element element) {
        servlet.setLoadOnStartup(Integer.parseInt(element.getTextContent().replaceAll("\\s", "")));
    }

    private void servletClass(Element element) {
        servlet.setClassName(element.getTextContent().replaceAll("\\s", ""));
    }

    private void servletName(Element element) {
        servlet.setName(element.getTextContent().replaceAll("\\s", ""));
    }

    private void startServlet() {
        servlet = new ServletDescriptor();
    }
}
