package info.fmro.betty.entities;

public class HttpErrorResponseAccountException {

    private String exceptionname;
    private AccountAPINGException AccountAPINGException;

    public HttpErrorResponseAccountException() {
    }

    public synchronized String getExceptionname() {
        return exceptionname;
    }

    public synchronized void setExceptionname(String exceptionname) {
        this.exceptionname = exceptionname;
    }

    public synchronized AccountAPINGException getAccountAPINGException() {
        return AccountAPINGException;
    }

    public synchronized void setAccountAPINGException(AccountAPINGException AccountAPINGException) {
        this.AccountAPINGException = AccountAPINGException;
    }
}
