/*
 * Simple Declarative Language (SDL) for Java
 * Copyright 2005 Ikayzo, inc.
 *
 * This program is free software. You can distribute or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by
 * the Free Software Foundation.
 *
 * This program is distributed AS IS and WITHOUT WARRANTY. OF ANY KIND,
 * INCLUDING MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, contact the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.singingbush.sdl;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SDL unit tests.  Why aren't these JUnit tests?  Mostly because being self
 * contained makes them easy to port to other languages.
 *
 * @author Daniel Leuck
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
public class SdlTest {

	private static PrintWriter out = new PrintWriter(System.out, true);

	// Tag datastructure tests
	private static final String TAG_WRITE_PARSE = "Tag Write Parse";

	// Basic Types Tests
	private static final String STRING_DECLARATIONS = "String Declarations";
	private static final String CHARACTER_DECLARATIONS = "Character Declarations";
	private static final String NUMBER_DECLARATIONS = "Number Declarations";
	private static final String BOOLEAN_DECLARATIONS = "Boolean Declarations";
	private static final String NULL_DECLARATION = "Null Declaration";
	private static final String DATE_DECLARATIONS = "Date Declarations";
	private static final String TIME_SPAN_DECLARATIONS = "Time Span Declarations";
	private static final String DATE_TIME_DECLARATIONS = "Date Time Declarations";
	private static final String BINARY_DECLARATIONS = "Binary Declarations";

	// Structure Tests
	private static final String EMPTY_TAG = "Empty Tag";
	private static final String VALUES = "Values";
	private static final String ATTRIBUTES = "Attributes";
	private static final String VALUES_AND_ATTRIBUTES = "Values and Attributes";
	private static final String CHILDREN = "Children";
	private static final String NAMESPACES = "Namespaces";

    @Test
	public void testTag() {
		out.println("Doing basic Tag tests...");

		// Test to make sure Tag ignores the order in which attributes are
		// added.
		final Tag t1 = new Tag("test");
		t1.setAttribute("foo", new SdlValue<>("bar", SdlType.STRING));
		t1.setAttribute("john", new SdlValue<>("doe", SdlType.STRING));

        final Tag t2 = new Tag("test");
		t2.setAttribute("john", new SdlValue<>("doe", SdlType.STRING));
		t2.setAttribute("foo", new SdlValue<>("bar", SdlType.STRING));

		// Tag should ignore the order in which attributes are added
		assertEquals(t1, t2);

        final SdlValue<String> item = new SdlValue<>("item", SdlType.STRING);
        t2.setValue(item);

        // tags with different structures should return false from .equals()
		assertNotEquals(t1, t2);

		t2.removeValue(item);
		t2.setAttribute("another", new SdlValue<>("attribute", SdlType.STRING));

		// tags with different structures should return false from .equals()
		assertNotEquals(t1, t2);

		out.println(" Checking attributes namespaces...");

		t2.setAttribute("name", new SdlValue<>("bill", SdlType.STRING));
		t2.setAttribute("private", "smoker", new SdlValue<>(true, SdlType.BOOLEAN));
		t2.setAttribute("public", "hobby", new SdlValue<>("hiking", SdlType.STRING));
		t2.setAttribute("private", "nickname", new SdlValue<>("tubby", SdlType.STRING));

        final SortedMap<String, Object> namespacedAttribs = t2.getAttributesForNamespace("private");

        assertEquals(namespacedAttribs, new TreeMap<String,Object>(map("smoker", true, "nickname", "tubby")));
	}

	////////////////////////////////////////////////////////////////////////////
	// Basic Types Tests
	////////////////////////////////////////////////////////////////////////////

    @Test
	public void testStrings() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root = new Tag("root").read(inputStream);

        out.println("Doing String tests...");
		out.println(" Doing basic tests including new line handling...");

		assertEquals(root.getChild("string1").getValue(), "hello");
		assertEquals(root.getChild("string2").getValue(), "hi");
		assertEquals(root.getChild("string3").getValue(), "aloha");
		assertEquals(root.getChild("string4").getValue(), "hi there");
		assertEquals(root.getChild("string5").getValue(), "hi there joe");
		assertEquals(root.getChild("string6").getValue(), "line1\nline2");
		assertEquals(root.getChild("string7").getValue(), "line1\nline2");
		assertEquals(root.getChild("string8").getValue(), "line1\nline2\nline3");
		assertEquals(root.getChild("string9").getValue(),
				"Anything should go in this line without escapes \\ \\\\ \\n " +
				"\\t \" \"\" ' ''");
		assertEquals(root.getChild("string10").getValue(), "escapes \"\\\n\t");

		out.println(" Checking unicode strings...");
		assertEquals(root.getChild("japanese").getValue(), "\u65e5\u672c\u8a9e");
		assertEquals(root.getChild("korean").getValue(), "\uc5ec\ubcf4\uc138\uc694");
		assertEquals(root.getChild("russian").getValue(),
				"\u0437\u0434\u0440\u0430\u0432\u0441\u0442\u0432\u0443\u043b\u0442\u0435");

		out.println(" More new line tests...");
        assertTrue(
            ((String)root.getChild("xml").getValue())
            .contains("<text>Hi there!</text>")
        );
		assertEquals(root.getChild("line_test").getValue(),
				"\nnew line above and below\n");
	}

	@Test
	public void testCharacters() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing character tests...");
		assertEquals(root.getChild("char1").getValue(), 'a');
		assertEquals(root.getChild("char2").getValue(), 'A');
		assertEquals(root.getChild("char3").getValue(), '\\');
		assertEquals(root.getChild("char4").getValue(), '\n');
		assertEquals(root.getChild("char5").getValue(), '\t');
		assertEquals(root.getChild("char6").getValue(), '\'');
		assertEquals(root.getChild("char7").getValue(), '"');

		out.println(" Doing unicode character tests...");
		assertEquals(root.getChild("char8").getValue(), '\u65e5');
		assertEquals(root.getChild("char9").getValue(), '\uc5ec');
		assertEquals(root.getChild("char10").getValue(), '\u0437');
	}

	@Test
	public void testNumbers() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing number tests...");

		out.println(" Testing ints...");
		assertEquals(root.getChild("int1").getValue(), 0);
		assertEquals(root.getChild("int2").getValue(), 5);
		assertEquals(root.getChild("int3").getValue(), -100);
		assertEquals(root.getChild("int4").getValue(), 234253532);

		out.println(" Testing longs...");
		assertEquals(root.getChild("long1").getValue(), 0L);
		assertEquals(root.getChild("long2").getValue(), 5L);
		assertEquals(root.getChild("long3").getValue(), 5L);
		assertEquals(root.getChild("long4").getValue(), 3904857398753453453L);

		out.println(" Testing floats...");
		assertEquals(root.getChild("float1").getValue(), 1F);
		assertEquals(root.getChild("float2").getValue(), .23F);
		assertEquals(root.getChild("float3").getValue(), -.34F);

		out.println(" Testing doubles...");
		assertEquals(root.getChild("double1").getValue(), 2D);
		assertEquals(root.getChild("double2").getValue(), -.234D);
		assertEquals(root.getChild("double3").getValue(), 2.34D);

		out.println(" Testing decimals (BigDouble in Java)...");
		assertEquals(root.getChild("decimal1").getValue(),
				new BigDecimal("0"));
		assertEquals(root.getChild("decimal2").getValue(),
				new BigDecimal("11.111111"));
		assertEquals(root.getChild("decimal3").getValue(),
				new BigDecimal("234535.3453453453454345345341242343"));
	}

	@Test
	public void testBooleans() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing boolean tests...");

		assertEquals(root.getChild("light-on").getValue(), true);
		assertEquals(root.getChild("light-off").getValue(), false);
		assertEquals(root.getChild("light1").getValue(), true);
		assertEquals(root.getChild("light2").getValue(), false);
	}

	@Test
	public void testNull() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing null test...");

		assertNull(root.getChild("nothing").getValue());
	}

	@Test
	public void testDates() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing date tests...");

		assertEquals(root.getChild("date1").getValue(),
				getDate(2005,12,31));
		assertEquals(root.getChild("date2").getValue(),
				getDate(1882,5,2));
		assertEquals(root.getChild("date3").getValue(),
				getDate(1882,5,2));
		assertEquals(root.getChild("_way_back").getValue(),
				getDate(582,9,16));
	}

	@Test
	public void testTimeSpans() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root=new Tag("root").read(inputStream);

		out.println("Doing time span tests...");

		assertEquals(root.getChild("time1").getValue(),
            Duration.ofHours(12).plusMinutes(30));
		assertEquals(root.getChild("time2").getValue(),
            Duration.ofHours(24));
		assertEquals(root.getChild("time3").getValue(),
            Duration.ofHours(1));
		assertEquals(root.getChild("time4").getValue(),
            Duration.ofHours(1));
		assertEquals(root.getChild("time5").getValue(),
            Duration.ofHours(12).plusMinutes(30).plusSeconds(2));
		assertEquals(root.getChild("time6").getValue(),
            Duration.ofHours(12).plusMinutes(30).plusSeconds(23));

		assertEquals(
		    Duration.ofHours(12).plusMinutes(30).plusSeconds(23).plusMillis(1),
            root.getChild("time7").getValue());

		assertEquals(
		    Duration.ofHours(12).plusMinutes(30).plusSeconds(23).plusMillis(12),
            root.getChild("time8").getValue());

		assertEquals(
		    Duration.ofHours(12).plusMinutes(30).plusSeconds(23).plusMillis(123),
            root.getChild("time9").getValue());

		out.println(" Checking time spans with days...");

		assertEquals(
		    Duration.ofDays(34).plusHours(12).plusMinutes(30).plusSeconds(23).plusMillis(1),
            root.getChild("time10").getValue());

		assertEquals(
		    Duration.ofDays(1).plusHours(12).plusMinutes(30),
            root.getChild("time11").getValue());

		assertEquals(
		    Duration.ofDays(5).plusHours(12).plusMinutes(30).plusSeconds(23).plusMillis(123),
            root.getChild("time12").getValue());

		out.println(" Checking negative time spans...");
		assertEquals(root.getChild("time13").getValue(),
            Duration.ofHours(12).plusMinutes(30).plusSeconds(23).plusMillis(123).negated());
		assertEquals(root.getChild("time14").getValue(),
            Duration.ofDays(5).plusHours(12).plusMinutes(30).plusSeconds(23).plusMillis(123).negated());
	}

	@Test
	public void testDateTimes() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root=new Tag("root").read(inputStream);

		out.println("Doing date time tests...");

		assertEquals(
		    getDateTime(2005,12,31,12,30,0,0),
            root.getChild("date_time1").getValue());

		assertEquals(
		    getDateTime(1882,5,2,12,30,0,0),
            root.getChild("date_time2").getValue());

		assertEquals(
		    getDateTime(2005,12,31,1,0,0,0),
            root.getChild("date_time3").getValue());

		assertEquals(
		    getDateTime(1882,5,2,1,0,0,0),
            root.getChild("date_time4").getValue());

		assertEquals(
		    getDateTime(2005,12,31,12,30,23,12),
            root.getChild("date_time5").getValue());

		assertEquals(
		    getDateTime(1882,5,2,12,30,23,123),
            root.getChild("date_time6").getValue());

		out.println(" Checking timezones...");
		assertEquals(root.getChild("date_time7").getValue(),
				getDateTime(1882,5,2,12,30,23,123,"JST"));
		assertEquals(root.getChild("date_time8").getValue(),
				getDateTime(985,04,11,12,30,23,123,"PST"));
	}

	@Test
	public void testBinaries() throws Exception {
        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root=new Tag("root").read(inputStream);

		out.println("Doing binary tests...");

		assertTrue(
		    Arrays.equals(
                    (byte[]) root.getChild("hi").getValue(),
                    "hi".getBytes("UTF8")
            )
        );

		assertTrue(
		    Arrays.equals(
                (byte[]) root.getChild("png").getValue(),
                Base64.getDecoder()
                    .decode("iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAKnRFWHRDcmVhdGlvbiBUaW1l" +
                        "AERpIDQgTXJ6IDIwMDMgMDA6MjQ6MDQgKzAxMDDdSQ6OAAAAB3RJTUUH0wMEAAcllPlrJgAA" +
                        "AAlwSFlzAAAK8AAACvABQqw0mAAAAARnQU1BAACxjwv8YQUAAADQSURBVHjaY2CgEDCCyZn/" +
                        "3YHkDhL1ejCkM+5kgXJ2zDQmXueShwwMh9+ALWSEGcCQfhZIvHlDnAk8PAwMHBxgJtyAa7bX" +
                        "UdT8/cvA8Ps3hP7zB4FBYn/+vGbweqyJaoCmpiaKASDFv35BNMBoZMzwGKKOidJYoNgAuBdm" +
                        "naXQgHRKDfgagxD89w8S+iAaFICwGIHFAgjrHUczAByySAaAMEgDLBphhv7/D8EYLgDZhAxA" +
                        "mkAKYYbAMMwwDAOQXYDuDXRXgDC6AR7SW8jITNQAACjZgdj4VjlqAAAAAElFTkSuQmCC")
            )
        );
	}

	////////////////////////////////////////////////////////////////////////////
	// Structure Tests ( children)
	////////////////////////////////////////////////////////////////////////////

    @Test
	public void testEmptyTag() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing empty tag test...");

		assertEquals(root.getChild("empty_tag"), new Tag("empty_tag"));
	}

	@Test
	public void testValues() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing values tests...");

		assertEquals(list("hi"), root.getChild("values1").getValues());
		assertEquals(list("hi", "ho"), root.getChild("values2").getValues());
		assertEquals(list(1, "ho"), root.getChild("values3").getValues());
		assertEquals(list("hi", 5), root.getChild("values4").getValues());
		assertEquals(list(1, 2), root.getChild("values5").getValues());
		assertEquals(list(1, 2, 3), root.getChild("values6").getValues());

		assertEquals(
		    list(null,"foo",false, getDate(1980,12,5)),
            root.getChild("values7").getValues());

		assertEquals(
		    list(null, "foo", false, getDateTime(1980,12,5,12,30,0,0),
                "there", Duration.ofHours(15).plusMinutes(23).plusSeconds(12).plusMillis(234)),
            root.getChild("values8").getValues());

		assertEquals(
            list(null, "foo", false, getDateTime(1980,12,5,12,30,0,0),
                "there", getDateTime(1989,8,12,15,23,12,234,"JST")),
            root.getChild("values9").getValues());

		assertEquals(
            list(null, "foo", false, getDateTime(1980,12,5,12,30,0,0),
                "there", Duration.ofHours(15).plusMinutes(23).plusSeconds(12).plusMillis(234), "more stuff"),
            root.getChild("values10").getValues());

		assertEquals(
            list(null, "foo", false, getDateTime(1980,12,5,12,30,0,0),
                "there", Duration.ofDays(123).plusHours(15).plusMinutes(23).plusSeconds(12).plusMillis(234),
                "more stuff here"),
            root.getChild("values11").getValues());

		assertEquals(list(1,3), root.getChild("values12").getValues());
		assertEquals(list(1,3), root.getChild("values13").getValues());
		assertEquals(list(1,3), root.getChild("values14").getValues());
		assertEquals(list(1,2,4,5,6), root.getChild("values15").getValues());
		assertEquals(list(1,2,5), root.getChild("values16").getValues());
		assertEquals(list(1,2,5), root.getChild("values17").getValues());
		assertEquals(list(1,2,7), root.getChild("values18").getValues());
		assertEquals(list(1,3,5,7), root.getChild("values19").getValues());
		assertEquals(list(1,3,5), root.getChild("values20").getValues());
		assertEquals(list(1,3,5), root.getChild("values21").getValues());
		assertEquals(list("hi","ho","ho",5,"hi"), root.getChild("values22").getValues());
	}

	@Test
	public void testAttributes() throws Exception {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println("Doing attribute tests...");

		assertEquals(
            map("name", new SdlValue<>("joe", SdlType.STRING)),
            root.getChild("atts1").getAttributes());

		assertEquals(
            map("size", new SdlValue<>(5, SdlType.NUMBER)),
            root.getChild("atts2").getAttributes());

		assertEquals(
            map("name", new SdlValue<>("joe", SdlType.STRING),
                "size", new SdlValue<>(5, SdlType.NUMBER)),
            root.getChild("atts3").getAttributes());

		assertEquals(
            map("name", new SdlValue<>("joe", SdlType.STRING),
                "size", new SdlValue<>(5, SdlType.NUMBER),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN)),
            root.getChild("atts4").getAttributes());

		assertEquals(
            map("name", new SdlValue<>("joe", SdlType.STRING),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN)),
            root.getChild("atts5").getAttributes());

		assertEquals(
            map("name", new SdlValue<>("joe", SdlType.STRING),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN)),
            root.getChild("atts6").getAttributes());

		assertEquals(
            map("name", new SdlValue<>("joe", SdlType.STRING)),
            root.getChild("atts7").getAttributes());

		assertEquals(
            map("name", new SdlValue<>("joe", SdlType.STRING),
                "size", new SdlValue<>(5, SdlType.NUMBER),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN),
                "text", new SdlValue<>("hi", SdlType.STRING),
                "birthday", new SdlValue<>(getDate(1972,5,23), SdlType.DATE)),
            root.getChild("atts8").getAttributes());

        assertTrue(
                Arrays.equals(
                        (byte[]) root.getChild("atts9").getAttribute("key"),
                        "mykey".getBytes("utf8")
                )
        );
	}

	@Test
	public void testValuesAndAttributes() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root=new Tag("root").read(inputStream);

		out.println("Doing values and attributes tests...");

		assertEquals(
            list("joe"),
            root.getChild("valatts1").getValues());

		assertEquals(
            map("size", new SdlValue<>(5, SdlType.NUMBER)),
            root.getChild("valatts1").getAttributes());

		assertEquals(list("joe"),
            root.getChild("valatts2").getValues());
		assertEquals(map("size", new SdlValue<>(5, SdlType.NUMBER)),
            root.getChild("valatts2").getAttributes());

		assertEquals(list("joe"),
            root.getChild("valatts3").getValues());
		assertEquals(map("size", new SdlValue<>(5, SdlType.NUMBER)),
            root.getChild("valatts3").getAttributes());

		assertEquals(list("joe"),
            root.getChild("valatts4").getValues());

		assertEquals(
            map("size", new SdlValue<>(5, SdlType.NUMBER),
                "weight", new SdlValue<>(160, SdlType.NUMBER),
                "hat", new SdlValue<>("big", SdlType.STRING)),
            root.getChild("valatts4").getAttributes()
        );

		assertEquals(
            root.getChild("valatts5").getValues(),
            list("joe", "is a\n nice guy"));

		assertEquals(
            root.getChild("valatts5").getAttributes(),
            map("size", new SdlValue<>(5, SdlType.NUMBER),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN)));

		assertEquals(
            list("joe", "is a\n nice guy"),
            root.getChild("valatts6").getValues());

		assertEquals(
            map("size", new SdlValue<>(5, SdlType.NUMBER),
                "house", new SdlValue<>("big and\n blue", SdlType.STRING_MULTILINE)),
            root.getChild("valatts6").getAttributes());

		//////////

		assertEquals(root.getChild("valatts7").getValues(),
            list("joe", "is a\n nice guy"));

		assertEquals(root.getChild("valatts7").getAttributes(),
            map("size", new SdlValue<>(5, SdlType.NUMBER),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN)));

		assertEquals(list("joe", "is a\n nice guy"),
            root.getChild("valatts8").getValues());

		assertEquals(root.getChild("valatts8").getAttributes(),
            map("size", new SdlValue<>(5, SdlType.NUMBER),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN)));

		assertEquals(list("joe", "is a\n nice guy"),
            root.getChild("valatts9").getValues());

		assertEquals(map("size", new SdlValue<>(5, SdlType.NUMBER),
                "smoker", new SdlValue<>(false, SdlType.BOOLEAN)),
            root.getChild("valatts9").getAttributes());
	}

	@Test
	public void testChildren() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root=new Tag("root").read(inputStream);

		out.println("Doing children tests...");

		Tag parent = root.getChild("parent");

		assertEquals(parent.getChildren().size(), 2);
		assertEquals(parent.getChildren().get(1).getName(),
				"daughter");


		Tag grandparent = root.getChild("grandparent");

		assertEquals(grandparent.getChildren().size(), 2);
		// recursive fetch of children
		assertEquals(grandparent.getChildren(true).size(), 6);
		assertEquals(grandparent.getChildren("son", true).size(), 2);

		Tag grandparent2 = root.getChild("grandparent2");
		assertEquals(grandparent2.getChildren("child", true).size(), 5);
		assertEquals(getDate(1976,4,18),
            grandparent2.getChild("daughter", true).getAttribute("birthday"));

		Tag files = root.getChild("files");

		assertEquals(files.getChildrenValues("content"),
				list("c:/file1.txt", "c:/file2.txt", "c:/folder"));

		Tag matrix = root.getChild("matrix");

		assertEquals(matrix.getChildrenValues("content"),
				list(list(1,2,3),list(4,5,6)));
	}

    @Test
    public void testMatrix2() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

        final Tag t = root.getChild("matrix2");

        assertEquals(t.getChildrenValues("content"),
            list(list(1,2,3),list(4,5,6)));
    }

    @Test
    public void testMatrix3() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

        final Tag t = root.getChild("matrix3");

        assertEquals(t.getChildrenValues("content"),
            list(list(1,2,3),list(4,5,6)));
    }

    @Test
    public void testMatrix4() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

        final Tag t = root.getChild("matrix4");

        assertEquals(t.getChildrenValues("content"),
            list(list(1,2,3),list(4,5,6)));
    }

	@Test
    public void testSemicolonsWithTwoLines() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

        final Tag t = root.getChild("twoLines");

        assertEquals(2, t.getChildren().size());

        assertEquals(list("line1", "line2"),
            t.getChildrenValues("content"));
    }

    @Test
    public void testSemicolonsWithThreeLines() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

        Tag t = root.getChild("threeLines");

        assertEquals(3, t.getChildren().size());

        assertEquals(list("line1", "line2", "line3"),
            t.getChildrenValues("content"));
    }

    @Test
    public void testSemicolonsWithFourLines() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

        Tag t = root.getChild("fourLines");

        assertEquals(4, t.getChildren().size());

        assertEquals(list("line1", "line2", 1, 2),
            t.getChildrenValues("content"));
    }

    @Test
    public void testSemicolonsWithSixLines() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

        Tag t = root.getChild("sixLines");

        assertEquals(6, t.getChildren().size());

        assertEquals(list("line1", "line2", "line3", 1, 2, 3),
            t.getChildrenValues("content"));
    }

	@Test
	public void testNamespaces() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root=new Tag("root").read(inputStream);

		out.println("Doing namespaces tests...");

		assertEquals(root.getChildrenForNamespace("person", true)
				.size(), 8);

		final Tag t = root.getChild("grandparent3");

        assertEquals(2, t.getChildrenForNamespace("person", false).size());
        assertEquals(6, t.getChildrenForNamespace("person", true).size());

		// get only the attributes for Akiko in the public namespace
        assertEquals(
            map("name", "Akiko",
                "birthday", LocalDate.of(1976,4,18)),
            t.getChild("daughter", true).getAttributesForNamespace("public"));
	}

    @Test
    public void testBasicTypes() throws IOException, SDLParseException {
		out.println("Reading test_basic_types.sdl");

        final InputStreamReader inputStream = loadTestResource("test_basic_types.sdl");
        final Tag root = new Tag("root").read(inputStream);

        assertNotNull(root);

        // Write out the contents of a tag, read the output back in and
        // test for equality.  This is a very rigorous test for any non-trivial
        // file.  It tests the parsing, output, and .equals implementation.
        out.println(" Write out the tag and read it back in...");

        final Tag clonedTag = new Tag("test").read(root.toString());
        assertEquals(root, clonedTag.getChild("root"));
	}

    @Test
    public void testStructures() throws IOException, SDLParseException {

		out.println("Reading test_structures.sdl");

        final InputStreamReader inputStream = loadTestResource("test_structures.sdl");
        final Tag root = new Tag("root").read(inputStream);

		out.println(" Successfully read and parsed test_structures.sdl");

        assertNotNull(root);

        // Write out the contents of a tag, read the output back in and
        // test for equality.  This is a very rigorous test for any non-trivial
        // file.  It tests the parsing, output, and .equals implementation.
        out.println(" Write out the tag and read it back in...");

        final Tag clonedTag = new Tag("test").read(root.toString());
        assertEquals(root, clonedTag.getChild("root"));
	}

    private InputStreamReader loadTestResource(final String testResourceFile) throws UnsupportedEncodingException {
        final InputStream testData = this.getClass()
                .getClassLoader()
                .getResourceAsStream(testResourceFile);
        return new InputStreamReader(testData, StandardCharsets.UTF_8);
    }

	////////////////////////////////////////////////////////////////////////////
	// Utility methods
	////////////////////////////////////////////////////////////////////////////

	private static LocalDate getDate(int year, int month, int day) {
		return LocalDate.of(year, month, day);
	}

    private static LocalDateTime getDateTime(int year, int month, int day, int hour,
                                             int minute, int second, int millisecond) {
        return LocalDateTime.of(year, month, day, hour, minute, second, millisecond*1_000_000);
    }

	private static ZonedDateTime getDateTime(int year, int month, int day, int hour,
                                             int minute, int second, int millisecond, String timeZone) {
        final ZoneId zoneId = timeZone != null ? TimeZone.getTimeZone(timeZone).toZoneId() : ZoneId.systemDefault();
        return ZonedDateTime.of(year, month, day, hour, minute, second, millisecond*1_000_000, zoneId);
	}

	private static List list(Object... obs) {
		ArrayList list = new ArrayList();
		for(Object o:obs)
			list.add(o);
		return list;
	}

	/**
	 * Make a map from alternating key/value pairs
	 */
	private static Map map(Object... obs) {
		TreeMap map = new TreeMap();
		for(int i = 0; i < obs.length;)
			map.put(obs[i++], obs[i++]);
		return map;
	}
}
