package info.fmro.betty.betapi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

// TypeAdaptor for the Date class which can be given to Gson. Betfair's API-NG requires all dates to be serialized in ISO8601 UTC.
class ISO8601DateTypeAdapter
        implements JsonSerializer<Date>, JsonDeserializer<Date> {
    public static final String ISO_8601_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String ISO_8601_TIMEZONE = "UTC";
    private final DateFormat dateFormat;

    ISO8601DateTypeAdapter() {
        this.dateFormat = new SimpleDateFormat(ISO_8601_FORMAT_STRING);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone(ISO_8601_TIMEZONE));
    }

    @Override
    public synchronized JsonElement serialize(final Date src, final Type typeOfSrc, final JsonSerializationContext context) {
        return new JsonPrimitive(this.dateFormat.format(src));
    }

    @Override
    public synchronized Date deserialize(@NotNull final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
        try {
            return this.dateFormat.parse(json.getAsString());
        } catch (ParseException parseException) {
            throw new JsonParseException(parseException);
        }
    }
}
