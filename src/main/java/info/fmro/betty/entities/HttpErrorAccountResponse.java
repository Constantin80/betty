package info.fmro.betty.entities;

@SuppressWarnings("SpellCheckingInspection")
public class HttpErrorAccountResponse {
    private HttpErrorResponseAccountException detail;
    private String faultcode, faultstring;

    public synchronized HttpErrorResponseAccountException getDetail() {
        return this.detail;
    }

    public synchronized void setDetail(final HttpErrorResponseAccountException detail) {
        this.detail = detail;
    }

    public synchronized String getFaultcode() {
        return this.faultcode;
    }

    public synchronized void setFaultcode(final String faultcode) {
        this.faultcode = faultcode;
    }

    public synchronized String getFaultstring() {
        return this.faultstring;
    }

    public synchronized void setFaultstring(final String faultstring) {
        this.faultstring = faultstring;
    }
}
