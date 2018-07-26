package info.fmro.betty.stream.protocol;

import java.util.concurrent.FutureTask;

/**
 * Created by mulveyj on 07/07/2016.
 */
public class FutureResponse<T>
        extends FutureTask<T> {
    private static final Runnable NULL = new Runnable() {
        @Override
        public void run() {
        }
    };

    public FutureResponse() {
        super(NULL, null);
    }

    public synchronized void setResponse(T response) {
        set(response);
    }

    public synchronized void setException(Throwable t) {
        super.setException(t);
    }
}
