package info.fmro.betty.entities;

public class HttpErrorResponseAccountException {

    private String exceptionname;
    private AccountAPINGException AccountAPINGException;

    public HttpErrorResponseAccountException() {
    }

    public synchronized String getExceptionname() {
        return exceptionname;
    }

    public synchronized void setExceptionname(final String exceptionname) {
        this.exceptionname = exceptionname;
    }

    public synchronized AccountAPINGException getAccountAPINGException() {
        return AccountAPINGException;
    }

    public synchronized void setAccountAPINGException(final AccountAPINGException AccountAPINGException) {
        this.AccountAPINGException = AccountAPINGException;
    }
}
