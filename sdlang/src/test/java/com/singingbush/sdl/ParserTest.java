package com.singingbush.sdl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samael Bate (singingbush)
 *         created on 14/07/2017
 */
public class ParserTest {

    @Test
    public void testComments() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("comments.sdl");

        final List<Tag> tags = new Parser(inputStream).parse();

        assertEquals(1, tags.size());
        assertEquals("tag", tags.get(0).getName());
        assertFalse((Boolean) tags.get(0).getAttribute("bar"));
    }

    @Test
    public void testDatatypes() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("datatypes.sdl");

        final List<Tag> tags = new Parser(inputStream).parse();

        assertFalse(tags.isEmpty());
        assertEquals(19, tags.size());
    }

    @Test
    public void testMultiline() throws IOException, SDLParseException {
        final String text = "xml `\n" +
            "<root type=\"widget\">\n" +
            "\t<color red=\"255\" green=\"0\" blue=\"0\"/>\n" +
            "\t<text>Hi there!</text>\n" +
            "</root>\n" +
            "`";

        final List<Tag> tags = new Parser(text).parse();

        assertFalse(tags.isEmpty());
        assertEquals(1, tags.size());
        final Tag tag = tags.get(0);
        assertEquals("xml", tag.getName());
        assertEquals(1, tag.getValues().size());
        assertEquals("\n" +
            "<root type=\"widget\">\n" +
            "\t<color red=\"255\" green=\"0\" blue=\"0\"/>\n" +
            "\t<text>Hi there!</text>\n" +
            "</root>\n", tag.getValue());

        // A Tag::toString() should be able to recreate the original SDL
        assertEquals(text, tag.toString());
    }

    @Test
    public void testSemicolons() throws IOException, SDLParseException {
        final List<Tag> tags = new Parser("thing {\n    \"line1\";\"line2\"; \"line3\"\n    1; 2; 3;\n}").parse();

        assertFalse(tags.isEmpty());
        assertEquals(1, tags.size());
        final Tag tag = tags.get(0);

        final List<Tag> children = tag.getChildren();
        assertEquals(6, children.size());

        assertEquals(3L, children.stream().filter(c -> SdlType.STRING.equals(c.getSdlValue().getType())).count());
        assertEquals(3L, children.stream().filter(c -> SdlType.NUMBER.equals(c.getSdlValue().getType())).count());
    }

    @Test
    public void testDetails() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("details.sdl");

        final List<Tag> tags = new Parser(inputStream).parse();

        assertFalse(tags.isEmpty());
        assertEquals(7, tags.size());
        final Tag tag = new Tag("title");
        tag.addValue(new SdlValue<>("Some title", SdlType.STRING));
        assertTrue(tags.contains(tag));

        assertTrue(tags.contains(new Tag("this-is_a.valid$tag-name")));

        final Tag rendererOpts = new Tag("renderer", "options");
        rendererOpts.setValue(new SdlValue<>("invisible", SdlType.STRING));
        assertTrue(tags.contains(rendererOpts));

        final Tag physicsOpts = new Tag("physics", "options");
        physicsOpts.setValue(new SdlValue<>("nocollide", SdlType.STRING));
        assertTrue(tags.contains(physicsOpts));
    }

    @Test
    public void testExample() throws IOException, SDLParseException {
        final InputStreamReader inputStream = loadTestResource("example.sdl");

        final List<Tag> tags = new Parser(inputStream).parse();

        assertFalse(tags.isEmpty());
        assertEquals(6, tags.size());

        // bookmarks 12 15 188 1234
        final Tag bookmarks = tags.get(1);
        assertEquals(4, bookmarks.getValues().size());

        // author "Peter Parker" email="peter@example.org" active=true
        final Tag author = tags.get(2);
        assertEquals("author", author.getName());
        assertEquals("Peter Parker", author.getValue());
        assertEquals("peter@example.org", author.getAttribute("email"));
        assertEquals(true, author.getAttribute("active"));

        // namespaced
        final Tag contents = tags.get(3);
        assertEquals("contents", contents.getName());
        final Tag firstSection = contents.getChildren("section").get(0);
        assertEquals("First section", firstSection.getValue());
        assertEquals(2, firstSection.getChildren().size());
    }

    @Test
    public void testParserWithString() throws IOException, SDLParseException {
        final List<Tag> tags = new Parser("author \"Peter Parker\" email=\"peter@example.org\" active=true").parse();
        assertFalse(tags.isEmpty());
        assertEquals(1, tags.size());
        assertEquals("author", tags.get(0).getName());
    }

    private InputStreamReader loadTestResource(final String testResourceFile) throws UnsupportedEncodingException {
        final InputStream testData = this.getClass()
                .getClassLoader()
                .getResourceAsStream(testResourceFile);
        return new InputStreamReader(testData, "UTF-8");
    }
}
