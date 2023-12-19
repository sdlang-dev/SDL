package com.singingbush.sdl;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samael Bate (singingbush)
 * created on 20/05/18
 */
public class DateTimeTest {

    private static final String DATE_REGEX = "(\\d+\\/\\d+\\/\\d+)";
    private static final String TIME_REGEX = "(\\d+:\\d+(:\\d+)?(.\\d+)?)(-\\w+)?";
    private static final String DATETIME_REGEX = DATE_REGEX + " " + TIME_REGEX;

    @Test
    public void testRegex() {
        assertTrue("2005/12/31 12:30".matches(DATETIME_REGEX));
        assertTrue("1882/5/2 12:30".matches(DATETIME_REGEX));
        assertTrue("2005/12/31 1:00".matches(DATETIME_REGEX));
        assertTrue("1882/5/2 1:00".matches(DATETIME_REGEX));
        assertTrue("2005/12/31 12:30:23.12".matches(DATETIME_REGEX));
        assertTrue("1882/5/2 12:30:23.123".matches(DATETIME_REGEX));
        assertTrue("1882/5/2 12:30:23.123-JST".matches(DATETIME_REGEX));
        assertTrue("985/04/11 12:30:23.123-PST".matches(DATETIME_REGEX));
    }

    @Test
    public void test1() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time1 2005/12/31 12:30");

        assertEquals(
            LocalDateTime.of(2005, 12, 31, 12, 30),
            root.getChild("date_time1").getValue()
        );
    }

    @Test
    public void test2() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time2 1882/5/2 12:30");

        assertEquals(
            LocalDateTime.of(1882, 5, 2, 12, 30),
            root.getChild("date_time2").getValue()
        );
    }

    @Test
    public void test3() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time3 2005/12/31 1:00");

        assertEquals(
            LocalDateTime.of(2005, 12, 31, 1, 0),
            root.getChild("date_time3").getValue()
        );
    }

    @Test
    public void test4() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time4 1882/5/2 1:00");

        assertEquals(
            LocalDateTime.of(1882, 5, 2, 1, 0),
            root.getChild("date_time4").getValue()
        );
    }

    @Test
    public void test5() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time5 2005/12/31 12:30:23.12");

        assertEquals(
            LocalDateTime.of(2005, 12, 31, 12, 30, 23, 12*1_000_000),
            root.getChild("date_time5").getValue()
        );
    }

    @Test
    public void test6() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time6 1882/5/2 12:30:23.123");

        assertEquals(
            LocalDateTime.of(1882, 5, 2, 12, 30, 23, 123*1_000_000),
            root.getChild("date_time6").getValue()
        );

        assertEquals("date_time6 1882/5/2 12:30:23.123",
            root.getChild("date_time6").toString());
    }

    @Test
    public void test7() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time7 1882/5/2 12:30:23.123-JST");

        assertEquals(
            ZonedDateTime.of(1882, 5, 2, 12, 30, 23, 123*1_000_000, ZoneId.of("Asia/Tokyo")),
            root.getChild("date_time7").getValue()
        );

        assertEquals("date_time7 1882/5/2 12:30:23.123-JST",
            root.getChild("date_time7").toString());
    }

    @Test
    public void test8() throws SDLParseException {
        final Tag root = new Tag("root").read("date_time8 985/04/11 12:30:23.123-PST");

        assertEquals(
            ZonedDateTime.of(985, 4, 11, 12, 30, 23, 123*1_000_000, ZoneId.of("America/Los_Angeles")),
            root.getChild("date_time8").getValue()
        );

        assertEquals("date_time8 985/4/11 12:30:23.123-PST",
            root.getChild("date_time8").toString());
    }
}
