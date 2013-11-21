package org.gethydrated.swarm.core.deploy;

import org.gethydrated.swarm.core.deploy.xml.XMLParser;
import org.gethydrated.swarm.core.messages.deploy.Mapping;
import org.w3c.dom.Element;

/**
 *
 */
public class ServletMappingParser implements XMLParser<Mapping> {

    private Mapping mapping;

    private boolean complete = false;

    @Override
    public Mapping getResult() {
        return complete ? mapping : null;
    }

    @Override
    public void startElement(Element element) {
        switch (element.getNodeName()) {
            case "servlet-mapping": startMapping(); break;
            case "servlet-name": startName(element); break;
            case "url-pattern": startPattern(element); break;
        }
    }

    private void startPattern(Element element) {
        mapping.addPattern(element.getTextContent().replaceAll("\\s", ""));
    }

    private void startName(Element element) {
        mapping.setName(element.getTextContent().replaceAll("\\s", ""));
    }

    private void startMapping() {
        mapping = new Mapping();
    }

    @Override
    public void endElement(Element element) {
        if (element.getNodeName().equals("servlet-mapping")) {
            complete = true;
        }
    }
}
