package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleConnectionMonitorThread
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RescriptResponseHandler.class);
    private final PoolingHttpClientConnectionManager connMgr;

    public IdleConnectionMonitorThread(final PoolingHttpClientConnectionManager connMgr) {
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "idleConnectionMonitor");
                }

                connMgr.closeExpiredConnections();
                connMgr.closeIdleConnections(20, TimeUnit.MINUTES);

                Generic.threadSleepSegmented(60_000L, 100L, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside idleConnectionMonitor loop", throwable);
            }
        } // end while

        logger.info("idleConnectionMonitor thread ends");
    }
}
