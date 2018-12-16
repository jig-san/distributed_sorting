package util;

import java.util.logging.Level;

/**
 * This is a static function for logging, for convenience.
 */
public class Logger {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();

    public static void log(String msg, Object ...params) {
        logger.log(Level.INFO, String.format(msg, params));
    }
}
