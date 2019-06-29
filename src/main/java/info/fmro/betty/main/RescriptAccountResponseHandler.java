package info.fmro.betty.main;

import info.fmro.betty.entities.AccountAPINGException;
import info.fmro.betty.entities.HttpErrorAccountResponse;
import info.fmro.betty.entities.HttpErrorResponseAccountException;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RescriptAccountResponseHandler
        implements ResponseHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(RescriptAccountResponseHandler.class);
    public static final long defaultPrintExpiry = 10_000L;
    // private boolean tooMuchData;

    // public synchronized boolean isTooMuchData() {
    //     return tooMuchData;
    // }
    // public synchronized void setTooMuchData(boolean tooMuchData) {
    //     this.tooMuchData = tooMuchData;
    // }
    @Override
    public synchronized String handleResponse(final HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        final StatusLine statusLine = httpResponse.getStatusLine();
        final HttpEntity httpEntity = httpResponse.getEntity();
        String httpEntityString = httpEntity == null ? null : EntityUtils.toString(httpEntity, Generic.UTF8_CHARSET);
        final int statusCode = statusLine.getStatusCode();
        if (statusCode != 200) {
            // <head><body> This object may be found <a HREF="http://content.betfair.com/content/splash/unplanned/index.asp">here</a> </body>
            // <html><body><b>Http/1.1 Service Unavailable</b></body> </html>
            final boolean tempBetfairError = statusCode == 503 ||
                                             (httpEntityString != null &&
                                              (httpEntityString.contains("content.betfair.com/content/splash/unplanned/") || httpEntityString.contains("Http/1.1 Service Unavailable")));
            if (!tempBetfairError) {
                final HttpErrorAccountResponse httpErrorAccountResponse = JsonConverter.convertFromJson(httpEntityString, HttpErrorAccountResponse.class);
                if (httpErrorAccountResponse != null) {
                    final HttpErrorResponseAccountException httpErrorResponseAccountException = httpErrorAccountResponse.getDetail();
                    if (httpErrorResponseAccountException != null) {
                        @SuppressWarnings("ThrowableResultIgnored") final AccountAPINGException accountAPINGException = httpErrorResponseAccountException.getAccountAPINGException();

                        if (accountAPINGException != null) {
                            switch (accountAPINGException.getErrorCode()) {
                                case INVALID_SESSION_INFORMATION:
                                    Statics.needSessionToken.set(true);
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.INFO, "needing another session token, INVALID_SESSION_INFORMATION, call to api-ng failed: {} {}", httpEntityString, statusLine);
                                    Generic.threadSleep(100);
                                    break;
                                case INVALID_APP_KEY: // I see this error if I send an invalid session token
                                    Statics.needSessionToken.set(true);
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "needing another session token, INVALID_APP_KEY, call to api-ng failed: {} {}", httpEntityString, statusLine);
                                    Generic.threadSleep(100);
                                    break;
                                case NO_SESSION: // might be related to an invalid session token
                                    Statics.needSessionToken.set(true);
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "needing another session token, NO_SESSION, call to api-ng failed: {} {}", httpEntityString, statusLine);
                                    Generic.threadSleep(100);
                                    break;
                                case TOO_MANY_REQUESTS:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "too many concurrent requests, call to api-ng failed: {} {}", httpEntityString, statusLine);
                                    Generic.threadSleep(500);
                                    break;
                                case UNEXPECTED_ERROR:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "unexpected server error, call to api-ng failed: {} {}", httpEntityString, statusLine);
                                    Generic.threadSleep(100);
                                    break;
                                case NO_APP_KEY:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "NO_APP_KEY server error, call to api-ng failed: {} {}", httpEntityString, statusLine);
                                    Generic.threadSleep(500);
                                    break;
                                default:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "unsupported accountAPINGException errorCode: {}, call to api-ng failed: {} {}", Generic.objectToString(httpErrorAccountResponse),
                                                                      httpEntityString, statusLine);
                                    Generic.threadSleep(500);
                                    break;
                            } // end switch
                        } else { // no error parsing can be done; likely json parser exception
                            Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "accountAPINGException null for: {} {}", httpEntityString, statusLine);
                            Generic.threadSleep(100);
                        }
                    } else { // no error parsing can be done; likely json parser exception
                        Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "httpErrorResponseAccountException null for: {} {}", httpEntityString, statusLine);
                        Generic.threadSleep(100);
                    }
                } else { // no error parsing can be done; likely json parser exception
                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "httpErrorResponse null for: {} {}", httpEntityString, statusLine);
                    Generic.threadSleep(100);
                }
            } else {
                Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "tempBetfairError: {} {}", httpEntityString, statusLine);
                Generic.threadSleep(100);
            }

            httpEntityString = null; // error string not returned further, as error is not managed and not expected further
        } else { // there's no error present, httpEntityString will be returned further
        }

        return httpEntityString;
    }
}
