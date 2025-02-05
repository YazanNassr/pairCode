package com.code.pair.yazan.paircode.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextModificationTest {

    @Test
    void transformBasedOn_whenCurrentLeftOfPrevious_noShift() {
        TextModification previous = mod(10, 15, "hello");
        TextModification current = mod(0, 5, "x");

        current.transformBasedOn(previous);

        assertEquals(0, current.getStart());
        assertEquals(5, current.getEnd());
    }

    @Test
    void transformBasedOn_whenCurrentRightOfPrevious_shiftsByDiff() {
        TextModification previous = mod(0, 5, "helloworld", "alice");
        TextModification current = mod(10, 15, "x", "bob");

        current.transformBasedOn(previous);

        assertEquals(15, current.getStart());
        assertEquals(20, current.getEnd());
    }

    @Test
    void transformBasedOn_sameModifier_skipsTransform() {
        TextModification previous = mod(0, 5, "hello", "alice");
        TextModification current = mod(10, 15, "x", "alice");

        current.transformBasedOn(previous);

        assertEquals(10, current.getStart());
        assertEquals(15, current.getEnd());
    }

    private static TextModification mod(int start, int end, String newVal) {
        return mod(start, end, newVal, "bob");
    }

    private static TextModification mod(int start, int end, String newVal, String modifier) {
        TextModification modification = new TextModification();
        modification.setStart(start);
        modification.setEnd(end);
        modification.setNewVal(newVal);
        modification.setModifier(modifier);
        return modification;
    }
}
