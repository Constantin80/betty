package info.fmro.betty.betapi;

import info.fmro.shared.enums.ApiNgOperation;
import info.fmro.betty.threads.permanent.GetLiveMarketsThread;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"OverlyComplexClass", "UtilityClass"})
final class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    public static final AtomicLong lastErrorLogged = new AtomicLong();
    public static final long intervalBetweenLoggedErrors = Generic.MINUTE_LENGTH_MILLISECONDS << 1;
    public static final String HTTP_HEADER_X_APPLICATION = "X-Application";
    public static final String HTTP_HEADER_X_AUTHENTICATION = "X-Authentication";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_ACCEPT = "Accept";
    public static final String HTTP_HEADER_ACCEPT_CHARSET = "Accept-Charset";

    @Contract(pure = true)
    private HttpUtil() {
    }

    private static boolean canLogError() { // must always be placed last condition in the if, else it will update the variable and might not print the message
        final boolean canLog;
        final long currentTime = System.currentTimeMillis();
        synchronized (lastErrorLogged) {
            if (currentTime - lastErrorLogged.get() >= intervalBetweenLoggedErrors) {
                canLog = true;
                lastErrorLogged.set(currentTime);
            } else {
                canLog = false;
            }
        } // end synchronized
        return canLog;
    }

    private static void errorStamp() {
//        synchronized (lastErrorLogged) {
        lastErrorLogged.set(System.currentTimeMillis());
//        } // end synchronized
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    private static String sendPostRequest(final String paramString, @NotNull final String operationString, final String appKeyString, final String URLString, final RescriptResponseHandler rescriptResponseHandler) {
        String responseString = null;
        boolean notSuccessful;
        long errorCounter = 0L;
        final boolean isPlacingOrder = operationString.equals(ApiNgOperation.PLACEORDERS.getOperationName());

        do {
            try {
                final HttpPost httpPost = new HttpPost(URLString);
                httpPost.setHeader(HTTP_HEADER_CONTENT_TYPE, Statics.APPLICATION_JSON);
                httpPost.setHeader(HTTP_HEADER_ACCEPT, Statics.APPLICATION_JSON);
                httpPost.setHeader(HTTP_HEADER_ACCEPT_CHARSET, Generic.UTF8_CHARSET);
                httpPost.setHeader(HTTP_HEADER_X_APPLICATION, appKeyString);
                httpPost.setHeader(HTTP_HEADER_X_AUTHENTICATION, Statics.sessionTokenObject.getSessionToken()); // makes the ssoTokenString argument obsolete
                httpPost.setEntity(new StringEntity(paramString, Generic.UTF8_CHARSET));
                if (isPlacingOrder) {
                    httpPost.setConfig(Statics.placingOrdersConfig);
                } else {
                    httpPost.setConfig(Statics.fastConfig);
                }

                responseString = Statics.client.execute(httpPost, rescriptResponseHandler);
                notSuccessful = responseString == null;
                if ((notSuccessful && errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) || Statics.debugLevel.check(3, 179)) {
                    logger.warn("responseString null in sendPostRequest, errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString);
                }
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                logger.error("STRANGE unsupportedEncodingException in sendPostRequest", unsupportedEncodingException);
                Statics.mustStop.set(true);
                notSuccessful = true;
            } catch (ClientProtocolException clientProtocolException) {
                logger.error("STRANGE clientProtocolException in sendPostRequest", clientProtocolException);
                Statics.mustStop.set(true);
                notSuccessful = true;
            } catch (SocketTimeoutException socketTimeoutException) {
                if (isPlacingOrder || (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError())) {
                    logger.error("socketTimeoutException in sendPostRequest, errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString, socketTimeoutException);
                } else if (Statics.debugLevel.check(3, 123)) {
                    logger.warn("socketTimeoutException in sendPostRequest", socketTimeoutException);
                } else {
                    logger.warn("socketTimeoutException in sendPostRequest: {}", socketTimeoutException.toString());
                }
                notSuccessful = true;
            } catch (ConnectTimeoutException connectTimeoutException) {
                if (isPlacingOrder || (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError())) {
                    logger.error("connectTimeoutException in sendPostRequest, errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString, connectTimeoutException);
                } else if (Statics.debugLevel.check(3, 124)) {
                    logger.warn("connectTimeoutException in sendPostRequest", connectTimeoutException);
                } else {
                    logger.warn("connectTimeoutException in sendPostRequest: {}", connectTimeoutException.toString());
                }
                notSuccessful = true;
            } catch (UnknownHostException unknownHostException) {
                if (isPlacingOrder || (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError())) {
                    logger.error("unknownHostException in sendPostRequest, errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString, unknownHostException);
                } else if (Statics.debugLevel.check(3, 157)) {
                    logger.warn("unknownHostException in sendPostRequest", unknownHostException);
                } else {
                    logger.warn("unknownHostException in sendPostRequest: {}", unknownHostException.toString());
                }
                // Generic.threadSleep(500L); // avoid throttle, likely network connection failure
                notSuccessful = true;
            } catch (SocketException socketException) {
                if (isPlacingOrder || (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError())) {
                    logger.error("socketException in sendPostRequest, errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString, socketException);
                } else if (Statics.debugLevel.check(3, 175)) {
                    logger.warn("socketException in sendPostRequest", socketException);
                } else {
                    logger.warn("socketException in sendPostRequest: {}", socketException.toString());
                }
                notSuccessful = true;
            } catch (NoHttpResponseException noHttpResponseException) {
                if (isPlacingOrder || (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError())) {
                    logger.error("noHttpResponseException in sendPostRequest, errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString, noHttpResponseException);
                } else if (Statics.debugLevel.check(3, 184)) {
                    logger.warn("noHttpResponseException in sendPostRequest", noHttpResponseException);
                } else {
                    logger.warn("noHttpResponseException in sendPostRequest: {}", noHttpResponseException.toString());
                }
                notSuccessful = true;
                errorCounter--; // avoid anti-throttle for this exception
            } catch (IOException iOException) {
                if (isPlacingOrder || (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError())) {
                    logger.error("iOException in sendPostRequest, errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString, iOException);
                } else if (Statics.debugLevel.check(3, 176)) {
                    logger.warn("iOException in sendPostRequest", iOException);
                } else {
                    logger.warn("iOException in sendPostRequest: {}", iOException.toString());
                }
                notSuccessful = true;
            }
            if (notSuccessful && !rescriptResponseHandler.isTooMuchData() && !Statics.mustStop.get()) {
                if (!GetLiveMarketsThread.waitForSessionToken("sendPostRequest " + operationString)) {
                    errorCounter++;
                    Generic.threadSleep((errorCounter - 1L) * 100L); // avoid throttle, sessionToken might not have been needed; no sleep for first error
                }
            }
        } while (notSuccessful && !rescriptResponseHandler.isTooMuchData() && !Statics.mustStop.get());
        if (errorCounter > 10L) {
            errorStamp();
            logger.error("finishing sendPostRequest with errorCounter: {}, isPlacingOrder: {}, operationString: {}", errorCounter, isPlacingOrder, operationString);
        }

        return responseString;
    }

    @SuppressWarnings({"OverlyLongMethod", "OverlyComplexMethod"})
    private static String sendAccountPostRequest(final String paramString, final String operationString, final String appKeyString, final String URLString, final ResponseHandler<String> rescriptAccountResponseHandler) {
        String responseString = null;
        boolean notSuccessful;
        long errorCounter = 0L;

        do {
            try {
                final HttpPost httpPost = new HttpPost(URLString);
                httpPost.setHeader(HTTP_HEADER_CONTENT_TYPE, Statics.APPLICATION_JSON);
                httpPost.setHeader(HTTP_HEADER_ACCEPT, Statics.APPLICATION_JSON);
                httpPost.setHeader(HTTP_HEADER_ACCEPT_CHARSET, Generic.UTF8_CHARSET);
                httpPost.setHeader(HTTP_HEADER_X_APPLICATION, appKeyString);
                httpPost.setHeader(HTTP_HEADER_X_AUTHENTICATION, Statics.sessionTokenObject.getSessionToken()); // makes the ssoTokenString argument obsolete

                httpPost.setEntity(new StringEntity(paramString, Generic.UTF8_CHARSET));
                httpPost.setConfig(Statics.accountsApiConfig);

                responseString = Statics.client.execute(httpPost, rescriptAccountResponseHandler);
                notSuccessful = responseString == null;
                if ((notSuccessful && errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) || Statics.debugLevel.check(3, 180)) {
                    logger.warn("responseString null in sendAccountPostRequest, errorCounter: {}, operationString: {}", errorCounter, operationString);
                }
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                logger.error("STRANGE unsupportedEncodingException in sendAccountPostRequest", unsupportedEncodingException);
                Statics.mustStop.set(true);
                notSuccessful = true;
            } catch (ClientProtocolException clientProtocolException) {
                logger.error("STRANGE clientProtocolException in sendAccountPostRequest", clientProtocolException);
                Statics.mustStop.set(true);
                notSuccessful = true;
            } catch (SocketTimeoutException socketTimeoutException) {
                if (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) {
                    logger.error("socketTimeoutException in sendAccountPostRequest, errorCounter: {}, operationString: {}", errorCounter, operationString, socketTimeoutException);
                } else if (Statics.debugLevel.check(3, 125)) {
                    logger.warn("socketTimeoutException in sendAccountPostRequest", socketTimeoutException);
                } else {
                    logger.warn("socketTimeoutException in sendAccountPostRequest: {}", socketTimeoutException.toString());
                }
                notSuccessful = true;
            } catch (ConnectTimeoutException connectTimeoutException) {
                if (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) {
                    logger.error("connectTimeoutException in sendAccountPostRequest, errorCounter: {}, operationString: {}", errorCounter, operationString, connectTimeoutException);
                } else if (Statics.debugLevel.check(3, 126)) {
                    logger.warn("connectTimeoutException in sendAccountPostRequest", connectTimeoutException);
                } else {
                    logger.warn("connectTimeoutException in sendAccountPostRequest: {}", connectTimeoutException.toString());
                }
                notSuccessful = true;
            } catch (UnknownHostException unknownHostException) {
                if (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) {
                    logger.error("unknownHostException in sendAccountPostRequest, errorCounter: {}, operationString: {}", errorCounter, operationString, unknownHostException);
                } else if (Statics.debugLevel.check(3, 158)) {
                    logger.warn("unknownHostException in sendAccountPostRequest", unknownHostException);
                } else {
                    logger.warn("unknownHostException in sendAccountPostRequest: {}", unknownHostException.toString());
                }
                // Generic.threadSleep(500L); // avoid throttle, likely network connection failure
                notSuccessful = true;
            } catch (SocketException socketException) {
                if (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) {
                    logger.error("socketException in sendAccountPostRequest, errorCounter: {}, operationString: {}", errorCounter, operationString, socketException);
                } else if (Statics.debugLevel.check(3, 177)) {
                    logger.warn("socketException in sendAccountPostRequest", socketException);
                } else {
                    logger.warn("socketException in sendAccountPostRequest: {}", socketException.toString());
                }
                notSuccessful = true;
            } catch (NoHttpResponseException noHttpResponseException) {
                if (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) {
                    logger.error("noHttpResponseException in sendAccountPostRequest, errorCounter: {}, operationString: {}", errorCounter, operationString, noHttpResponseException);
                } else if (Statics.debugLevel.check(3, 185)) {
                    logger.warn("noHttpResponseException in sendAccountPostRequest", noHttpResponseException);
                } else {
                    logger.warn("noHttpResponseException in sendAccountPostRequest: {}", noHttpResponseException.toString());
                }
                notSuccessful = true;
                errorCounter--; // avoid anti-throttle for this exception
            } catch (IOException iOException) {
                if (errorCounter >= 10L && errorCounter % 10L == 0 && canLogError()) {
                    logger.error("iOException in sendAccountPostRequest, errorCounter: {}, operationString: {}", errorCounter, operationString, iOException);
                } else if (Statics.debugLevel.check(3, 178)) {
                    logger.warn("iOException in sendAccountPostRequest", iOException);
                } else {
                    logger.warn("iOException in sendAccountPostRequest: {}", iOException.toString());
                }
                notSuccessful = true;
            }
            if (notSuccessful && !Statics.mustStop.get()) {
                if (!GetLiveMarketsThread.waitForSessionToken("sendAccountPostRequest " + operationString)) {
                    errorCounter++;
                    Generic.threadSleep((errorCounter - 1L) * 100L); // avoid throttle, sessionToken might not have been needed
                }
            }
        } while (notSuccessful && !Statics.mustStop.get());
        if (errorCounter > 10L) {
            errorStamp();
            logger.error("finishing sendAccountPostRequest with errorCounter: {}, operationString: {}", errorCounter, operationString);
        }

        return responseString;
    }

    static String sendPostRequestAccountRescript(final String paramString, final String operationString, final String appKeyString, final ResponseHandler<String> rescriptAccountResponseHandler) {
        final String apiNgURLString = Statics.ACCOUNT_APING_URL + Statics.RESCRIPT_SUFFIX + operationString + "/";
        return sendAccountPostRequest(paramString, operationString, appKeyString, apiNgURLString, rescriptAccountResponseHandler);
    }

    static String sendPostRequestRescript(final String paramString, final String operationString, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final String apiNgURLString = Statics.APING_URL + Statics.RESCRIPT_SUFFIX + operationString + "/";
        return sendPostRequest(paramString, operationString, appKeyString, apiNgURLString, rescriptResponseHandler);
    }
}
