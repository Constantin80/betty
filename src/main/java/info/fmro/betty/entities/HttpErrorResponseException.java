package info.fmro.betty.entities;

public class HttpErrorResponseException {

    private String exceptionname;
    private APINGException APINGException;

    public HttpErrorResponseException() {
    }

    public synchronized String getExceptionname() {
        return exceptionname;
    }

    public synchronized void setExceptionname(String exceptionname) {
        this.exceptionname = exceptionname;
    }

    public synchronized APINGException getAPINGException() {
        return APINGException;
    }

    public synchronized void setAPINGException(APINGException APINGException) {
        this.APINGException = APINGException;
    }
}
