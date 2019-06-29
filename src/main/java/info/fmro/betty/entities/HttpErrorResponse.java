package info.fmro.betty.entities;

public class HttpErrorResponse {

    private HttpErrorResponseException detail;
    private String faultcode, faultstring;

    public HttpErrorResponse() {
    }

    public synchronized HttpErrorResponseException getDetail() {
        return detail;
    }

    public synchronized void setDetail(final HttpErrorResponseException detail) {
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
