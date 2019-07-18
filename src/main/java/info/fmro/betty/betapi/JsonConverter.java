package info.fmro.betty.betapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Date;

@SuppressWarnings("UtilityClass")
public final class JsonConverter {
    private static final Logger logger = LoggerFactory.getLogger(JsonConverter.class);

    // We needed to override the adapter for the Date class as Betfair's API-NG requires all dates to be serialized in ISO8601 UTC Just formatting the string to the ISO format does not adjust by the timezone on the Date instance during serialization.
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISO8601DateTypeAdapter()).create();
    public static final long defaultPrintExpiry = Generic.MINUTE_LENGTH_MILLISECONDS;

    @Contract(pure = true)
    private JsonConverter() {
    }

    // This method deserializes the specified Json into an object of the specified class.
    @SuppressWarnings({"MethodWithMultipleReturnPoints", "OverloadedMethodsWithSameNumberOfParameters"})
    @Nullable
    public static <T> T convertFromJson(final String toConvertString, final Class<T> clazz) {
        try {
            return gson.fromJson(toConvertString, clazz);
        } catch (JsonSyntaxException jsonSyntaxException) {
            if (Statics.debugLevel.check(3, 169)) {
                logger.error("jsonSyntaxException in convertFromJson Class {} for: {}", clazz, toConvertString, jsonSyntaxException);
            } else {
                Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "jsonSyntaxException in convertFromJson Class {} for: {}", clazz, toConvertString, jsonSyntaxException);
            }
            return null;
        }
    }

    // This method deserializes the specified Json into an object of the specified Type.
    @SuppressWarnings({"MethodWithMultipleReturnPoints", "OverloadedMethodsWithSameNumberOfParameters"})
    @Nullable
    static <T> T convertFromJson(final String toConvertString, final Type typeOfT) {
        try {
            return gson.fromJson(toConvertString, typeOfT);
        } catch (JsonSyntaxException jsonSyntaxException) {
            if (Statics.debugLevel.check(3, 170)) {
                logger.error("jsonSyntaxException in convertFromJson Type {} for: {}", typeOfT, toConvertString, jsonSyntaxException);
            } else {
                Generic.alreadyPrintedMap.logOnce(defaultPrintExpiry, logger, LogLevel.ERROR, "jsonSyntaxException in convertFromJson Type {} for: {}", typeOfT, toConvertString, jsonSyntaxException);
            }
            return null;
        }
    }

    // This method serializes the specified object into its equivalent Json representation.
    static String convertToJson(final Object toConvertObject) {
        return gson.toJson(toConvertObject);
    }
}
