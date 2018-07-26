package info.fmro.betty.stream.definitions;

public class PriceLadderDefinition {
    private PriceLadderType type;

    public synchronized PriceLadderType getType() {
        return type;
    }
}
