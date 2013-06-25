package org.gethydrated.swarm.launcher;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        try {
            throw new RuntimeException();
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static void fail(Throwable t) {
        if (t != null) {
            t.printStackTrace(System.err);
        }
        SystemExit.exit(ExitCode.FAIL);
    }
}
