package com.domain;

import com.singingbush.sdl.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests are in a different package to 'com.singingbush.sdl' to ensure that the API makes sense
 * @author Samael Bate (singingbush)
 * created on 06/06/18
 */
public class SdlangTest {

    @Test
    public void testParserConstructorWithReader() throws IOException, SDLParseException {
        final Reader reader = new StringReader("employee \"Mr Smith\" email=\"smith@domain.org\" active=true // comments");

        final List<Tag> tags = new Parser(reader).parse();

        assertEquals(1, tags.size());
        final Tag tag = tags.get(0);
        assertEquals("employee", tag.getName());
        assertEquals("Mr Smith", String.class.cast(tag.getValue()));
        assertEquals("Mr Smith", tag.getValue(String.class));
        assertEquals(2, tag.getAttributes().size());
        assertEquals("smith@domain.org", tag.getAttribute("email"));
        assertEquals(true, tag.getAttribute("active"));
    }

    @Test
    public void testParserConstructorWithString() throws IOException, SDLParseException {
        assertTrue(new Parser("").parse().isEmpty());

        final List<Tag> tags = new Parser("matrix {\n 8 64 87; 31 34 18 \n}").parse();

        assertEquals(1, tags.size());
        final Tag tag = tags.get(0);
        assertEquals("matrix", tag.getName());
        assertEquals(2, tag.getChildren().size());

        assertEquals(3, tag.getChildren().get(0).getValues().size());
        assertEquals(3, tag.getChildren().get(1).getValues().size());

        final Tag sameContent = SDL.tag("matrix")
            .withChildren(
                SDL.tag().withValues(SDL.value(8), SDL.value(64), SDL.value(87)).build(),
                SDL.tag().withValues(SDL.value(31), SDL.value(34), SDL.value(18)).build()
            )
            .build();

        assertEquals(sameContent, tag);

        assertEquals(tags, new Parser(sameContent.toString()).parse());
    }

    @Test
    public void testWritingSdl() {
        final LocalDateTime dateTime = LocalDateTime.now();

        final Tag child = new Tag("child"); // use SDL.tag() when constructor removed
        child.addValue(SDL.value(dateTime));
        child.addValue(SDL.value(541L));

        final Tag t = new Tag("my", "thing");
        t.addValue(SDL.value('g'));
        t.addChild(child);
        t.setAttribute("flt", SDL.value(0.0f));
        t.setAttribute("lng", SDL.value(1_000L));

        // Create a Tag the new way
        Tag tag = SDL.tag("thing")
            .withNamespace("my")
            .withComment("some comment that is only available\nwhen serialised using pretty print")
            .withValue(SDL.value('g'))
            .withChild(SDL.tag("child")
                .withComment("some line comment")
                .withValues(SDL.value(dateTime), SDL.value(541L))
                .build()
            )
            .withAttribute("flt", SDL.value(0.0f))
            .withAttribute("lng", SDL.value(1_000L))
            .build();

        // content should be same regardless of the new style allowing comments
        assertEquals(t, tag);

        assertEquals(1, tag.getValues().size());
        assertEquals(Character.class, tag.getValues().get(0).getClass());
        assertEquals(1, tag.getChildren().size());
        assertEquals(2, tag.getAttributes().size());
    }
}
