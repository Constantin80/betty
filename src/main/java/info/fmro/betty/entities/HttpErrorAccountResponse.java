package info.fmro.betty.entities;

public class HttpErrorAccountResponse {

    private HttpErrorResponseAccountException detail;
    private String faultcode, faultstring;

    public HttpErrorAccountResponse() {
    }

    public synchronized HttpErrorResponseAccountException getDetail() {
        return detail;
    }

    public synchronized void setDetail(final HttpErrorResponseAccountException detail) {
        this.detail = detail;
    }

    public synchronized String getFaultcode() {
        return faultcode;
    }

    public synchronized void setFaultcode(final String faultcode) {
        this.faultcode = faultcode;
    }

    public synchronized String getFaultstring() {
        return faultstring;
    }

    public synchronized void setFaultstring(final String faultstring) {
        this.faultstring = faultstring;
    }
}
