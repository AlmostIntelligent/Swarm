package org.gethydrated.swarm.messages;

/**
 *
 */
public class RegisterApp {

    private String contextName;

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    @Override
    public String toString() {
        return "RegisterApp{" +
                "contextName='" + contextName + '\'' +
                "}";
    }
}
