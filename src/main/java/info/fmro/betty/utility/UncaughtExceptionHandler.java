package info.fmro.betty.utility;

import info.fmro.betty.objects.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;

public class UncaughtExceptionHandler
        implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

    // Implements Thread.UncaughtExceptionHandler.uncaughtException()
    @Override
    public void uncaughtException(final Thread thread, final Throwable throwable) {
        if (Statics.mustStop.get() && throwable instanceof RejectedExecutionException) {
            logger.warn("Crashed thread while mustStop: {} {} {} {}", thread.getName(), thread.getId(), thread, throwable.toString());
        } else if (throwable instanceof ThreadDeath) {
            logger.warn("Crashed thread: {} {} {} {}", thread.getName(), thread.getId(), thread, throwable.toString());
        } else {
            logger.error("Crashed thread: {} {} {}", thread.getName(), thread.getId(), thread, throwable);
        }
    }
}
