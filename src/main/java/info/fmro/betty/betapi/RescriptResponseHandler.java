package info.fmro.betty.betapi;

import info.fmro.betty.entities.APINGException;
import info.fmro.betty.entities.HttpErrorResponse;
import info.fmro.betty.entities.HttpErrorResponseException;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RescriptResponseHandler
        implements ResponseHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(RescriptResponseHandler.class);
    public static final long defaultPrintExpiry = 10_000L;
    public static final String idPattern = "\"requestUUID\":\"";
    private boolean tooMuchData;

    public synchronized boolean isTooMuchData() {
        return this.tooMuchData;
    }

    public synchronized void setTooMuchData(final boolean tooMuchData) {
        this.tooMuchData = tooMuchData;
    }

    @SuppressWarnings("OverlyNestedMethod")
    @Override
    public synchronized String handleResponse(@NotNull final HttpResponse response)
            throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity httpEntity = response.getEntity();
        @Nullable String httpEntityString = httpEntity == null ? null : EntityUtils.toString(httpEntity, Generic.UTF8_CHARSET);
        final int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) { // there's no error present, httpEntityString will be returned further
        } else {
            // <head><body> This object may be found <a HREF="http://content.betfair.com/content/splash/unplanned/index.asp">here</a> </body>
            // <html><body><b>Http/1.1 Service Unavailable</b></body> </html>
            final boolean tempBetfairError = statusCode == 503 || (httpEntityString != null && (httpEntityString.contains("content.betfair.com/content/splash/unplanned/") || httpEntityString.contains("Http/1.1 Service Unavailable")));
            if (tempBetfairError) {
                Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "tempBetfairError: {} {}", httpEntityString, statusLine);
                Generic.threadSleep(100L);
            } else {
                final HttpErrorResponse httpErrorResponse = JsonConverter.convertFromJson(httpEntityString, HttpErrorResponse.class);
                if (httpErrorResponse != null) {
                    final HttpErrorResponseException httpErrorResponseException = httpErrorResponse.getDetail();
                    if (httpErrorResponseException != null) {
                        final APINGException aPINGException = httpErrorResponseException.getAPINGException();
                        if (aPINGException != null) {
                            final String modifiedHttpEntityString = Generic.removeSubstring(httpEntityString, idPattern, "\"", "erased"); // I remove an id portion, for logOnce; else httpEntityString's id is different all the time
                            switch (aPINGException.getErrorCode()) {
                                case INVALID_SESSION_INFORMATION:
                                    Statics.needSessionToken.set(true);
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.INFO, "needing another session token, INVALID_SESSION_INFORMATION, call to api-ng failed: {} {}", modifiedHttpEntityString, statusLine);
                                    Generic.threadSleep(100L);
                                    break;
                                case INVALID_APP_KEY: // I see this error if I send an invalid session token
                                    Statics.needSessionToken.set(true);
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "needing another session token, INVALID_APP_KEY, call to api-ng failed: {} {}", modifiedHttpEntityString, statusLine);
                                    Generic.threadSleep(100L);
                                    break;
                                case NO_SESSION: // might be related to an invalid session token
                                    Statics.needSessionToken.set(true);
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "needing another session token, NO_SESSION, call to api-ng failed: {} {}", modifiedHttpEntityString, statusLine);
                                    Generic.threadSleep(100L);
                                    break;
                                case TOO_MANY_REQUESTS:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "too many concurrent requests, call to api-ng failed: {} {}", modifiedHttpEntityString, statusLine);
                                    Generic.threadSleep(500L);
                                    break;
                                case TOO_MUCH_DATA:
                                    this.tooMuchData = true;
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "too much data requested, call to api-ng failed: {} {}", modifiedHttpEntityString, statusLine);
                                    break;
                                case UNEXPECTED_ERROR:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.WARN, "unexpected server error, call to api-ng failed: {} {}", modifiedHttpEntityString, statusLine);
                                    Generic.threadSleep(100L);
                                    break;
                                case NO_APP_KEY:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "NO_APP_KEY server error, call to api-ng failed: {} {}", modifiedHttpEntityString, statusLine);
                                    Generic.threadSleep(500L);
                                    break;
                                default:
                                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "unsupported aPINGException errorCode: {}, call to api-ng failed: {} {}", Generic.objectToString(httpErrorResponse), modifiedHttpEntityString,
                                                                      statusLine);
                                    Generic.threadSleep(500L);
                                    break;
                            } // end switch
                        } else { // no error parsing can be done; likely json parser exception
                            Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "aPINGException null for: {} {}", httpEntityString, statusLine);
                            Generic.threadSleep(100L);
                        }
                    } else { // no error parsing can be done; likely json parser exception
                        Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "httpErrorResponseException null for: {} {}", httpEntityString, statusLine);
                        Generic.threadSleep(100L);
                    }
                } else { // no error parsing can be done; likely json parser exception
                    Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "httpErrorResponse null for: {} {}", httpEntityString, statusLine);
                    Generic.threadSleep(100L);
                }
            }
            httpEntityString = null; // error string not returned further, as error is not managed and not expected further
        }

        return httpEntityString;
    }
}
