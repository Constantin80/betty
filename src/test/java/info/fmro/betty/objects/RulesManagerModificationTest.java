package info.fmro.betty.objects;

import info.fmro.shared.enums.RulesManagerModificationCommand;
import info.fmro.betty.main.ApiDefault;
import info.fmro.shared.stream.objects.RulesManagerModification;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class RulesManagerModificationTest
        extends ApiDefault {
    @Test
    void testConstructorPrimitives() {
        final Serializable[] firstArray = {1, 1.1, true}, secondArray = new Serializable[3];
        for (int i = 0; i < 3; i++) {
            secondArray[i] = SerializationUtils.clone(firstArray[i]);
        }
        assertArrayEquals(secondArray, firstArray, "array clone test");

        final RulesManagerModification object = new RulesManagerModification(RulesManagerModificationCommand.setMaxLayOdds, 1, 1.1, true);
        final Serializable[] array = object.getArray();
        assertArrayEquals(new Serializable[]{1, 1.1, true}, array, "constructor test");
    }
}
