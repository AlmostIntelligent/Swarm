package org.gethydrated.swarm.launcher;

/**
 * Wrapper for System.exit() calls.
 * Can be used to alter behavior of exit calls
 * for unit or integration tests.
 *
 * @author Christian Kulpa
 * @since 1.0.0
 */
public class SystemExit {

    private static Exit exit;

    public interface Exit {
        void exit(int exitcode);
    }

    public static void setExit(Exit exit) {
        SystemExit.exit = exit;
    }

    public static Exit getExit() {
        return (exit == null) ? new StandardExit() : exit;
    }

    public static void exit(int exitcode) {
        getExit().exit(exitcode);
    }

    private static class StandardExit implements Exit {

        @Override
        public void exit(int exitcode) {
            System.exit(exitcode);
        }
    }

}
