package com.singingbush.sdl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Samael Bate (singingbush)
 *         created on 13/07/2017
 */
public class DataTypesTest {

    @Test // "String \"with escape support\""
    public void testStringsWithEscapeSupport() {
        final SdlValue value = SDL.value("\"String\t\"with escape support\"\"");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(String.class));
        assertEquals("\"String\\t\\\"with escape support\\\"\"", value.getText());
    }

    @Test // `String "without escape support"`
    public void testStringsWithoutEscapeSupport() {
        final SdlValue value = SDL.value("`String \"without escape support\"`");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(String.class));
        assertEquals("`String \"without escape support\"`", value.getText());
    }

    // Numbers

    @Test // 10 == 32-bit numbers (Integer)
    public void test32bitIntegers() {
        final SdlValue value = SDL.value("10");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(Integer.class));
    }

    @Test // 10.5 == 64-bit floating point numbers (Double)
    public void test64bitDouble() {
        final SdlValue value = SDL.value("10.5");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(Double.class));
    }

    @Test // 10L == 64-bit numbers (Long)
    public void test64bitLong() {
        final SdlValue value = SDL.value("10L");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(Long.class));
    }

    @Test // 10BD == 128-bit numbers (BigDecimal)
    public void test128bitBigDecimal() {
        final SdlValue value = SDL.value("10BD");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(BigDecimal.class));
    }

    @Test // 10.5f == 32-bit float
    public void test32bitFloat() {
        final SdlValue value = SDL.value("10.5f");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(Float.class));
    }

    @Test
    public void testBooleans() {
        assertTrue(sdlBoolean("true"));
        assertTrue(sdlBoolean("on"));

        assertFalse(sdlBoolean("false"));
        assertFalse(sdlBoolean("off"));
    }

    @Test // "null" should be parsed as a null
    public void testNull() {
        final SdlValue value = SDL.value("null");
        assertNull(value.getValue());
        assertEquals(SdlType.NULL, value.getType());
        assertEquals("null", value.getText());
    }

    @Test // 2015/12/06 12:00:00.000-UTC // Date/time value (UTC timezone)
    public void testDateTimeWithZoneInfo() {
        final SdlValue value = SDL.value("2015/12/06 12:00:00.000-UTC");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(ZonedDateTime.class));
    }

    @Test // 2015/12/06 12:00:00.000 // Date/time value (local timezone)
    public void testDateTimeLocal() {
        final SdlValue value = SDL.value("2015/12/06 12:00:00.000");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(LocalDateTime.class));
    }

    @Test // 2015/12/06 // Date value
    public void testDate() {
        final SdlValue value = SDL.value("2015/12/06");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(LocalDate.class));
    }

    @Test // time
    public void testTime() {
        final SdlValue value = SDL.value("12:14:34");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(Duration.class));
        assertEquals("PT12H14M34S", value.getValue().toString());
        assertEquals("12:14:34", value.getText());
    }

    @Test // time
    public void testTime_withMillis() {
        final SdlValue value = SDL.value("12:14:34.123");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(Duration.class));
        //assertEquals("PT12H14M34.123S", value.getValue().toString());
        assertEquals("PT12H14M34.123S", value.getValue().toString());
        assertEquals("12:14:34.123", value.getText());
    }

    @Test // time
    public void testTimeSpan_DaysHoursMinsSecs() {
        final SdlValue value = SDL.value("2d:12:10:22");
        assertNotNull(value);
        // maybe this should be period
        assertTrue(value.getValue().getClass().isAssignableFrom(Duration.class));
        assertEquals("PT60H10M22S", value.getValue().toString());
        assertEquals("2d:12:10:22", value.getText());
    }

    @Test // base 64
    public void testBinaryData() {
        final SdlValue value = SDL.value("[sdf789GSfsb2+3324sf2]");
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(byte[].class));
    }

    private Boolean sdlBoolean(final String text) {
        final SdlValue value = SDL.value(text);
        assertNotNull(value);
        assertTrue(value.getValue().getClass().isAssignableFrom(Boolean.class));
        return Boolean.class.cast(value.getValue());
    }

}
