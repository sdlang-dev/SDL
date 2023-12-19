package com.singingbush.sdl;

import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samael Bate (singingbush)
 * created on 18/05/18
 */
public class TimeSpanTest {

    private static final String TIMESPAN_REGEX = "-?(\\d+d:)?(\\d+:\\d+:\\d+)(.\\d+)?";

    @Test
    public void testTimeSpanRegex() {
        assertTrue("12:30:00".matches(TIMESPAN_REGEX));
        assertTrue("24:00:00".matches(TIMESPAN_REGEX));
        assertTrue("1:00:00".matches(TIMESPAN_REGEX));
        assertTrue("1:0:0".matches(TIMESPAN_REGEX));
        assertTrue("12:30:2".matches(TIMESPAN_REGEX));
        assertTrue("12:30:23".matches(TIMESPAN_REGEX));
        assertTrue("12:30:23.1".matches(TIMESPAN_REGEX));
        assertTrue("12:30:23.12".matches(TIMESPAN_REGEX));
        assertTrue("12:30:23.123".matches(TIMESPAN_REGEX));
        assertTrue("34d:12:30:23.1".matches(TIMESPAN_REGEX));
        assertTrue("1d:12:30:0".matches(TIMESPAN_REGEX));
        assertTrue("5d:12:30:23.123".matches(TIMESPAN_REGEX));
        assertTrue("-12:30:23.123".matches(TIMESPAN_REGEX));
        assertTrue("-5d:12:30:23.123".matches(TIMESPAN_REGEX));
    }

    @Test
    public void testTime1() throws SDLParseException {
        final Tag root = new Tag("root").read("time1 12:30:00");

        assertEquals("time1 12:30:00", root.getChild("time1").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(12).plusMinutes(30), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime2() throws SDLParseException {
        final Tag root = new Tag("root").read("time2 24:00:00");

        assertEquals("time2 24:00:00", root.getChild("time2").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(24), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime3() throws SDLParseException {
        final Tag root = new Tag("root").read("time3 1:00:00");

        assertEquals("time3 01:00:00", root.getChild("time3").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(1), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime4() throws SDLParseException {
        final Tag root = new Tag("root").read("time4 1:0:0");

        assertEquals("time4 01:00:00", root.getChild("time4").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(1), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime5() throws SDLParseException {
        final Tag root = new Tag("root").read("time5 12:30:2");

        assertEquals("time5 12:30:02", root.getChild("time5").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(12).plusMinutes(30).plusSeconds(2), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime6() throws SDLParseException {
        final Tag root = new Tag("root").read("time6 12:31:23");

        assertEquals("time6 12:31:23", root.getChild("time6").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(12).plusMinutes(31).plusSeconds(23), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime7() throws SDLParseException {
        final Tag root = new Tag("root").read("time7 12:45:23.1");

        assertEquals("time7 12:45:23.001", root.getChild("time7").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(12).plusMinutes(45).plusSeconds(23).plusMillis(1), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime8() throws SDLParseException {
        final Tag root = new Tag("root").read("time8 12:33:23.12");

        assertEquals("time8 12:33:23.012", root.getChild("time8").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(12).plusMinutes(33).plusSeconds(23).plusMillis(12), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime9() throws SDLParseException {
        final Tag root = new Tag("root").read("time9 8:13:15.123");

        assertEquals("time9 08:13:15.123", root.getChild("time9").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(8).plusMinutes(13).plusSeconds(15).plusMillis(123), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime10() throws SDLParseException {
        final Tag root = new Tag("root").read("time10 34d:12:53:33.1");

        assertEquals("time10 34d:12:53:33.001", root.getChild("time10").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofDays(34).plusHours(12).plusMinutes(53).plusSeconds(33).plusMillis(1), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime11() throws SDLParseException {
        final Tag root = new Tag("root").read("time11 1d:12:30:0");

        assertEquals("time11 1d:12:30:00", root.getChild("time11").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofDays(1).plusHours(12).plusMinutes(30), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime12() throws SDLParseException {
        final Tag root = new Tag("root").read("time12 5d:11:18:24.123");

        assertEquals("time12 5d:11:18:24.123", root.getChild("time12").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofDays(5).plusHours(11).plusMinutes(18).plusSeconds(24).plusMillis(123), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime13() throws SDLParseException {
        final Tag root = new Tag("root").read("time13 -12:36:44.753");

        assertEquals("time13 -12:36:44.753", root.getChild("time13").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(Duration.ofHours(12).plusMinutes(36).plusSeconds(44).plusMillis(753).negated(), sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testTime14() throws SDLParseException {
        final Tag root = new Tag("root").read("time14 -5d:12:8:4.753");

        assertEquals("time14 -5d:12:08:04.753", root.getChild("time14").toString());

        final SdlValue sdlValue = shouldHaveSingleDurationValue(root);
        assertEquals(
            Duration.ofDays(5).plusHours(12).plusMinutes(8).plusSeconds(4).plusMillis(753).negated(),
            sdlValue.getValue());
        shouldConvertToString(root);
    }


    private SdlValue shouldHaveSingleDurationValue(final Tag tag) {
        assertEquals(1, tag.getChildren().size());
        final SdlValue sdlValue = tag.getChildren().get(0).getSdlValue();
        assertEquals(SdlType.DURATION, sdlValue.getType());
        assertEquals(Duration.class, sdlValue.getValue().getClass());
        return sdlValue;
    }

    private void shouldConvertToString(final Tag tag) throws SDLParseException {
        final Tag clonedTag = new Tag("test").read(tag.toString());
        assertEquals(tag, clonedTag.getChild(tag.getName()));
    }
}
