package info.fmro.betty.stream.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import info.fmro.betty.stream.definitions.ConnectionMessage;
import info.fmro.betty.stream.definitions.MarketChangeMessage;
import info.fmro.betty.stream.definitions.OrderChangeMessage;
import info.fmro.betty.stream.definitions.StatusMessage;

@SuppressWarnings("MarkerInterface")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "op",
        visible = true)
@JsonSubTypes({
                      @JsonSubTypes.Type(value = ConnectionMessage.class, name = "connection"),
                      @JsonSubTypes.Type(value = StatusMessage.class, name = "status"),
                      @JsonSubTypes.Type(value = MarketChangeMessage.class, name = "mcm"),
                      @JsonSubTypes.Type(value = OrderChangeMessage.class, name = "ocm"),
              })
public interface MixInResponseMessage {
}
