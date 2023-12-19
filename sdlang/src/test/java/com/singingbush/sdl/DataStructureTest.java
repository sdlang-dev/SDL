package com.singingbush.sdl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samael Bate (singingbush)
 * created on 19/05/18
 */
public class DataStructureTest {


    @Test
    public void testValues1() throws SDLParseException {
        final Tag root = new Tag("root").read("values1 \"hi\"");

        final Tag t = root.getChild("values1");

        // values:
        assertEquals(1, t.getValues().size());
        assertEquals("hi", t.getValue());

        // attributes:
        assertEquals(0, t.getAttributes().size());

        assertEquals("values1 \"hi\"", t.toString());
    }

    @Test
    public void testValues2() throws SDLParseException {
        final Tag root = new Tag("root").read("values2 \"hi\" \"ho\"");

        final Tag t = root.getChild("values2");

        // values:
        assertEquals(2, t.getValues().size());
        assertEquals("hi", t.getValue(0));
        assertEquals("ho", t.getValue(1));

        // attributes:
        assertEquals(0, t.getAttributes().size());

        assertEquals("values2 \"hi\" \"ho\"", t.toString());
    }

    @Test
    public void testValues3() throws SDLParseException {
        final Tag root = new Tag("root").read("values3 1 \"ho\"");

        final Tag t = root.getChild("values3");

        // values:
        assertEquals(2, t.getValues().size());
        assertEquals(1, t.getValue(0));
        assertEquals("ho", t.getValue(1));

        // attributes:
        assertEquals(0, t.getAttributes().size());

        assertEquals("values3 1 \"ho\"", t.toString());
    }

    @Test
    public void testValues8() throws SDLParseException {
        final Tag root = new Tag("root").read("myTest null \"foo\" false 1980/12/5 12:30 `there` 15:23:12.234");

        final Tag t = root.getChild("myTest");

        // values:
        assertEquals(6, t.getValues().size());
        assertEquals(null, t.getValue(0));
        assertEquals("foo", t.getValue(1));
        assertEquals(false, t.getValue(2));
        assertEquals(LocalDateTime.of(1980, 12, 5, 12, 30), t.getValue(3));
        assertEquals("there", t.getValue(4));
        assertEquals(Duration.ofHours(15).plusMinutes(23).plusSeconds(12).plusMillis(234), t.getValue(5));

        // attributes:
        assertEquals(0, t.getAttributes().size());

        //assertEquals("values3 1 \"ho\"", t.toString());
    }

    @Test
    public void testValues9() throws SDLParseException {
        final Tag root = new Tag("root").read("myTest null \"foo\" false 1980/12/5 12:30 `there` 1989/8/12 15:23:12.234-JST");

        final Tag t = root.getChild("myTest");

        // values:
        assertEquals(6, t.getValues().size());
        assertEquals(null, t.getValue(0));
        assertEquals("foo", t.getValue(1));
        assertEquals(false, t.getValue(2));
        assertEquals(LocalDateTime.of(1980, 12, 5, 12, 30), t.getValue(3));
        assertEquals("there", t.getValue(4));
        assertEquals(ZonedDateTime.of(1989, 8, 12, 15, 23, 12, 234*1_000_000, ZoneId.of(ZoneId.SHORT_IDS.get("JST"))),
            t.getValue(5));

        // attributes:
        assertEquals(0, t.getAttributes().size());

        //assertEquals("values3 1 \"ho\"", t.toString());
    }

    @Test
    public void testValatts1() throws SDLParseException {
        final Tag t = new Tag("root")
            .read("valatts1 \"joe\" size=5")
            .getChild("valatts1");

        // values:
        assertEquals(1, t.getValues().size());
        assertEquals("joe", t.getValue());

        // attributes:
        assertEquals(1, t.getAttributes().size());
        assertEquals(5, t.getAttribute("size"));

        assertEquals("valatts1 \"joe\" size=5", t.toString());
    }

    @Test
    public void testValatts2() throws SDLParseException {
        final Tag root = new Tag("root").read("valatts2 \"joe\" size=5 #comment...");

        final Tag t = root.getChild("valatts2");

        // values:
        assertEquals(1, t.getValues().size());
        assertEquals("joe", t.getValue());

        // attributes:
        assertEquals(1, t.getAttributes().size());
        assertEquals(5, t.getAttribute("size"));

        // should be able to recreate original SDL (without comment)
        assertEquals("valatts2 \"joe\" size=5", t.toString());
    }

    @Test
    public void testValatts3() throws SDLParseException {
        final Tag root = new Tag("root").read("valatts3 \"joe\" size=5#comment...");

        final Tag t = root.getChild("valatts3");

        // values:
        assertEquals(1, t.getValues().size());
        assertEquals("joe", t.getValue());

        // attributes:
        assertEquals(1, t.getAttributes().size());
        assertEquals(5, t.getAttribute("size"));

        // should be able to recreate original SDL (without comment)
        assertEquals("valatts3 \"joe\" size=5", t.toString());
    }

    @Test
    public void testValatts4() throws SDLParseException {
        final Tag root = new Tag("root").read("valatts4 \"joe\" size=5 weight=160 hat=\"big\"");

        final Tag t = root.getChild("valatts4");
        assertEquals(1, t.getValues().size());
        assertEquals("joe", t.getValue());

        assertEquals(3, t.getAttributes().size());
        assertEquals(5, t.getAttribute("size"));
        assertEquals(160, t.getAttribute("weight"));
        assertEquals("big", t.getAttribute("hat"));
    }

    @Test
    public void testValatts5() throws SDLParseException {
        final Tag root = new Tag("root").read("valatts5 \"joe\" `is a\n nice guy` size=5 smoker=false");

        final Tag t = root.getChild("valatts5");
        assertEquals(2, t.getValues().size());
        assertEquals("joe", t.getValue());
        assertEquals("is a\n nice guy", t.getValue(1));

        assertEquals(2, t.getAttributes().size());
        assertEquals(5, t.getAttribute("size"));
        assertEquals(false, t.getAttribute("smoker"));
    }

    @Test
    public void testValatts6() throws SDLParseException {
        final Tag root = new Tag("root").read("valatts6 \"joe\" `is a\n nice guy` size=5 house=`big and\n blue`");

        final Tag t = root.getChild("valatts6");
        assertEquals(2, t.getValues().size());
        assertEquals("joe", t.getValue());

        assertEquals(2, t.getAttributes().size());
        assertEquals(5, t.getAttribute("size"));
        assertEquals("big and\n blue", t.getAttribute("house"));
    }

    @Test
    public void testNamespaces() throws SDLParseException, IOException {
        final String sdl = "person:grandparent3 name=\"Harold\" age=93 {\n" +
            "  person:child name=\"Sabrina\" age=93 smoker=false { #comment here...\n" +
            "    person:son name=\"Joe\" -- a bit odd...\n" +
            "    person:daughter public:name=\"Akiko\" private:smokes=true \\\n" +
            "      public:birthday=1976/04/18\n" +
            "    dog name=\"Spot\"\n" +
            "    /* cat name=\"Scratches\" */\n" +
            "  }\n" +
            "  /*\n" +
            "  person:drunk_uncle funny_drunk=false {\n" +
            "    person:angry_daughter name=\"Jill\"\n" +
            "  }\n" +
            "  */\n" +
            "  person:child name=\"Jose\" {\n" +
            "    person:son\n" +
            "    person:daughter\n" +
            "  }\n" +
            "}";

        final Tag root = new Parser(sdl).parse().get(0);

        final List<Tag> children = root.getChildrenForNamespace("person", false);
        assertEquals(2, children.size());

        final List<Tag> childrenAndGrandchildren = root.getChildrenForNamespace("person", true);
        assertEquals(6, childrenAndGrandchildren.size());

        assertEquals(root, new Parser(root.toString()).parse().get(0));

        assertEquals(map("name", "Akiko", "birthday", LocalDate.of(1976,4,18)),
            root.getChild("daughter", true).getAttributesForNamespace("public"));
    }

    private static Map map(Object... obs) {
        TreeMap map = new TreeMap();
        for(int i = 0; i < obs.length;)
            map.put(obs[i++], obs[i++]);
        return map;
    }
}
