package com.code.pair.yazan.paircode.dsa.rope;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RopeTest {

    @Test
    void replace_insertsTextAtRange() {
        Rope rope = Rope.from("hello world");

        Rope updated = rope.replace(5, 6, ",");

        assertEquals("hello,world", updated.toString());
    }

    @Test
    void from_emptyString_hasZeroLength() {
        Rope rope = Rope.from("");
        assertEquals(0, rope.length());
    }
}
