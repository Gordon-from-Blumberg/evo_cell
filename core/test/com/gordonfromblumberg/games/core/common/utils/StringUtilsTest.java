package com.gordonfromblumberg.games.core.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.gordonfromblumberg.games.core.common.utils.StringUtils.*;

public class StringUtilsTest {
    @Test
    void formatTest() {
        Assertions.assertEquals("", format(""));
        Assertions.assertEquals("test", format("test"));
        Assertions.assertEquals("Hello, World!", format("Hello, #!", "World"));
        Assertions.assertEquals("Hello, null!", format("Hello, #!", (Object) null));
        Assertions.assertEquals("5 is bigger than 2", format("# is bigger than #", 5, 2));
        Assertions.assertEquals("Length is 3m.", format("Length is ##.", 3, "m"));
        Assertions.assertEquals("ff4500", format("###", "ff", "45", "00"));

        Assertions.assertThrows(NullPointerException.class, () -> format(null));
        Assertions.assertThrows(NullPointerException.class, () -> format(null, ""));

        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> format("#"));
        Assertions.assertEquals("No arguments passed for template '#'", e.getMessage());

        e = Assertions.assertThrows(IllegalArgumentException.class, () -> format("Length is ##.", 1));
        Assertions.assertEquals("Incorrect number of arguments: 1 passed for template 'Length is ##.'", e.getMessage());

        e = Assertions.assertThrows(IllegalArgumentException.class, () -> format("Hello, #!", "World", 1));
        Assertions.assertEquals("Incorrect number of arguments: 2 passed for template 'Hello, #!'", e.getMessage());
    }

    @Test
    void padLeftTest() {
        Assertions.assertEquals("00", padLeft(0, 2));
        Assertions.assertEquals("001", padLeft(1, 3));
        Assertions.assertEquals("123", padLeft(123, 2));
    }

    @Test
    void floatToStringTest() {
        Assertions.assertEquals("1.2", floatToString(1.2f, 1));
        Assertions.assertEquals("1.20", floatToString(1.2f, 2));
        Assertions.assertEquals("1", floatToString(1.2f, 0));
        Assertions.assertEquals("-1.2", floatToString(-1.2f, 1));
        Assertions.assertEquals("-1.20", floatToString(-1.2f, 2));
        Assertions.assertEquals("-1", floatToString(-1.2f, 0));
        Assertions.assertEquals("1.000", floatToString(1, 3));
        Assertions.assertEquals("0.001", floatToString(0.001f, 3));
        Assertions.assertEquals("0.00", floatToString(0.001f, 2));
        Assertions.assertEquals("0.0", floatToString(0.001f, 1));
        Assertions.assertEquals("0.002", floatToString(0.0016f, 3));
        Assertions.assertEquals("0.82", floatToString(0.821564f, 2));
        Assertions.assertEquals("-0.82", floatToString(-0.8164f, 2));
        Assertions.assertEquals("123.82", floatToString(123.8164f, 2));
        Assertions.assertEquals("-123.82", floatToString(-123.8164f, 2));
    }

    @Test
    void isBlankTest() {
        Assertions.assertTrue(isBlank(null));
        Assertions.assertTrue(isBlank(""));
        Assertions.assertTrue(isBlank(" "));
        Assertions.assertTrue(isBlank("\n"));
        Assertions.assertFalse(isBlank("foo"));
        Assertions.assertFalse(isBlank("  foo"));
        Assertions.assertFalse(isBlank("\nfoo"));
        Assertions.assertFalse(isBlank("null"));
    }
}
