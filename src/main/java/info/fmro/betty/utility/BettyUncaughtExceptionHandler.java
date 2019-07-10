package info.fmro.betty.utility;

import info.fmro.betty.objects.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;

public class BettyUncaughtExceptionHandler
        implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(BettyUncaughtExceptionHandler.class);

    // Implements Thread.CustomUncaughtExceptionHandler.uncaughtException()
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        if (Statics.mustStop.get() && e instanceof RejectedExecutionException) {
            logger.warn("Crashed thread while mustStop: {} {} {} {}", t.getName(), t.getId(), t, e.toString());
        } else if (e instanceof ThreadDeath) {
            logger.warn("Crashed thread: {} {} {} {}", t.getName(), t.getId(), t, e.toString());
        } else {
            logger.error("Crashed thread: {} {} {}", t.getName(), t.getId(), t, e);
        }
    }
}
