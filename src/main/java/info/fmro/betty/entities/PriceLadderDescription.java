package info.fmro.betty.entities;

import info.fmro.betty.enums.PriceLadderType;

import java.io.Serializable;

class PriceLadderDescription
        implements Serializable {
    private static final long serialVersionUID = -6000098777316431103L;
    @SuppressWarnings("unused")
    private PriceLadderType type; // The type of price ladder.
}
