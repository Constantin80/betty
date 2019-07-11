package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

class IdleConnectionMonitorThread
        implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(IdleConnectionMonitorThread.class);
    private final PoolingHttpClientConnectionManager connMgr;

    @Contract(pure = true)
    IdleConnectionMonitorThread(final PoolingHttpClientConnectionManager connMgr) {
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "idleConnectionMonitor");
                }

                this.connMgr.closeExpiredConnections();
                this.connMgr.closeIdleConnections(20, TimeUnit.MINUTES);

                Generic.threadSleepSegmented(60_000L, 100L, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside idleConnectionMonitor loop", throwable);
            }
        } // end while

        logger.info("idleConnectionMonitor thread ends");
    }
}
