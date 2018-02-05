package info.fmro.betty.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UncaughtExceptionHandler
        implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

    // Implements Thread.UncaughtExceptionHandler.uncaughtException()
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof ThreadDeath) {
            logger.warn("Crashed thread: {} {} {} {}", thread.getName(), thread.getId(), thread, throwable.toString());
        } else {
            logger.error("Crashed thread: {} {} {}", thread.getName(), thread.getId(), thread, throwable);
        }
    }
}
