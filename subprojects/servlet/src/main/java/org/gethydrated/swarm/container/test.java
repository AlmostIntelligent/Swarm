package org.gethydrated.swarm.container;

import java.util.regex.Pattern;

/**
 *
 */
public class test {

    public static void main(String[] args) {
        String request = "/hello/";
        String mapping = "/hello/";
        mapping = mapping.replaceAll("\\*", ".*");
        Pattern p = Pattern.compile(mapping);

        System.out.print(p.matcher(request).matches());

    }
}
