package com.gttcgf.nanoscan.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PasswordUtilsTest {
    @Test
    public void convertsByteArrayWithTerminator() {
        byte[] input = new byte[] { 'h', 'e', 'l', 'l', 'o', 0 };
        String result = PasswordUtils.getBytetoString(input);
        assertEquals("hello", result);
    }

    @Test
    public void convertsByteArrayWithoutTerminator() {
        byte[] input = new byte[] { 'w', 'o', 'r', 'l', 'd' };
        String result = PasswordUtils.getBytetoString(input);
        assertEquals("world", result);
    }
}
