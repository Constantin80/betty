package info.fmro.betty.entities;

@SuppressWarnings({"NonExceptionNameEndsWithException", "SpellCheckingInspection"})
public class HttpErrorResponseException {
    private String exceptionname;
    private APINGException APINGException;

    public synchronized String getExceptionname() {
        return this.exceptionname;
    }

    public synchronized void setExceptionname(final String exceptionname) {
        this.exceptionname = exceptionname;
    }

    public synchronized APINGException getAPINGException() {
        return this.APINGException;
    }

    public synchronized void setAPINGException(final APINGException APINGException) {
        this.APINGException = APINGException;
    }
}
