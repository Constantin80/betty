package info.fmro.betty.stream.definitions;

import info.fmro.betty.main.OrdersThread;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Side { // BACK, LAY;
    B,
    L;

    private static final Logger logger = LoggerFactory.getLogger(OrdersThread.class);

    public synchronized info.fmro.betty.enums.Side toStandardSide() {
        @Nullable final info.fmro.betty.enums.Side returnValue;
        if (this == B) {
            returnValue = info.fmro.betty.enums.Side.BACK;
        } else if (this == L) {
            returnValue = info.fmro.betty.enums.Side.LAY;
        } else {
            logger.error("strange unsupported value in toStandardSide: {}", this);
            returnValue = null;
        }

        return returnValue;
    }
}
