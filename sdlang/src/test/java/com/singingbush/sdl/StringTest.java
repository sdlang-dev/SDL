package com.singingbush.sdl;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samael Bate (singingbush)
 * created on 19/05/18
 */
public class StringTest {

    @Test
    public void testString1() throws SDLParseException {
        final Tag root = new Tag("root").read("string1 \"hello\"");

        assertEquals("string1 \"hello\"", root.getChild("string1").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("hello", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString2() throws SDLParseException {
        final Tag root = new Tag("root").read("string2 \"hi\"");

        assertEquals("string2 \"hi\"", root.getChild("string2").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("hi", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString3() throws SDLParseException {
        final Tag root = new Tag("root").read("string3 `aloha`");

        assertEquals("string3 `aloha`", root.getChild("string3").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING_MULTILINE);
        assertEquals("aloha", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString4() throws SDLParseException {
        final Tag root = new Tag("root").read("string4 \"hi \\\n    there\"");

        assertEquals("string4 \"hi there\"", root.getChild("string4").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("hi there", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString5() throws SDLParseException {
        final Tag root = new Tag("root").read("string5 \"hi \\\n" +
            "    there \\\n" +
            "    joe\"");

        assertEquals("string5 \"hi there joe\"", root.getChild("string5").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("hi there joe", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString6() throws SDLParseException {
        final Tag root = new Tag("root").read("string6 \"line1\\nline2\"");

        assertEquals("string6 \"line1\\nline2\"", root.getChild("string6").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("line1\nline2", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString7() throws SDLParseException {
        final Tag root = new Tag("root").read("string7 `line1\nline2`");

        assertEquals("string7 `line1\nline2`", root.getChild("string7").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING_MULTILINE);
        assertEquals("line1\nline2", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString8() throws SDLParseException {
        final Tag root = new Tag("root").read("string8 `line1\nline2\nline3`");

        assertEquals("string8 `line1\nline2\nline3`", root.getChild("string8").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING_MULTILINE);
        assertEquals("line1\nline2\nline3", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString9_literalShouldntEscape() throws SDLParseException {
        final Tag root = new Tag("root").read("string9 `Anything should go in this line without escapes \\ \\\\ \\n \\t \" \"\" ' ''`");

        assertEquals("string9 `Anything should go in this line without escapes \\ \\\\ \\n \\t \" \"\" ' ''`", root.getChild("string9").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING_MULTILINE);
        assertEquals(
            "Anything should go in this line without escapes \\ \\\\ \\n \\t \" \"\" ' ''",
            sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString10_ShouldEscape() throws SDLParseException {
        final Tag root = new Tag("root").read("string10 \"escapes \\\"\\\\\\n\\t\"");

        assertEquals("string10 \"escapes \\\"\\\\\\n\\t\"", root.getChild("string10").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("escapes \"\\\n\t", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString11_unicodeJapanese() throws SDLParseException {
        final Tag root = new Tag("root").read("japanese \"日本語\"");

        assertEquals("japanese \"日本語\"", root.getChild("japanese").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("日本語", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString12_unicodeKorean() throws SDLParseException {
        final Tag root = new Tag("root").read("korean \"여보세요\"");

        assertEquals("korean \"여보세요\"", root.getChild("korean").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("여보세요", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString13_unicodeRussian() throws SDLParseException {
        final Tag root = new Tag("root").read("russian \"здравствулте\"");

        assertEquals("russian \"здравствулте\"", root.getChild("russian").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING);
        assertEquals("здравствулте", sdlValue.getValue());
        shouldConvertToString(root);
    }

    @Test
    public void testString14_literalContainingXml() throws SDLParseException {
        final String xml = "\n" +
            "<root type=\"widget\">\n" +
            "\t<color red=\"255\" green=\"0\" blue=\"0\"/>\n" +
            "\t<text>Hi there!</text>\n" +
            "</root>\n";

        final String text = String.format("xml `%s`", xml);

        final Tag root = new Tag("root").read(text);

        assertEquals(text, root.getChild("xml").toString());

        final SdlValue sdlValue = shouldHaveSingleStringValue(root, SdlType.STRING_MULTILINE);
        assertEquals(xml, sdlValue.getValue());
        shouldConvertToString(root);
    }

    private SdlValue shouldHaveSingleStringValue(final Tag tag, SdlType expectedType) {
        assertEquals(1, tag.getChildren().size());
        final SdlValue sdlValue = tag.getChildren().get(0).getSdlValue();
        assertEquals(expectedType, sdlValue.getType());
        assertEquals(String.class, sdlValue.getValue().getClass());
        return sdlValue;
    }

    private void shouldConvertToString(final Tag tag) throws SDLParseException {
        final Tag clonedTag = new Tag("test").read(tag.toString());
        assertEquals(tag, clonedTag.getChild(tag.getName()));
    }
}
