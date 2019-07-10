package info.fmro.betty.entities;

@SuppressWarnings({"NonExceptionNameEndsWithException", "SpellCheckingInspection"})
public class HttpErrorResponseAccountException {
    private String exceptionname;
    private AccountAPINGException AccountAPINGException;

    public synchronized String getExceptionname() {
        return this.exceptionname;
    }

    public synchronized void setExceptionname(final String exceptionname) {
        this.exceptionname = exceptionname;
    }

    public synchronized AccountAPINGException getAccountAPINGException() {
        return this.AccountAPINGException;
    }

    public synchronized void setAccountAPINGException(final AccountAPINGException AccountAPINGException) {
        this.AccountAPINGException = AccountAPINGException;
    }
}
