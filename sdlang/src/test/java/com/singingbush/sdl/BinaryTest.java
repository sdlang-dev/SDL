package com.singingbush.sdl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Samael Bate (singingbush)
 * created on 22/05/18
 */
public class BinaryTest {

    private static final String PNG_IMAGE = "[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]";

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get("target/image.png"));
    }

    @Test
    public void testUtf8String() throws IOException, SDLParseException {
        // The bytes are for: "japanese: 日本語, korean: 여보세요, and russian здравствулте";
        final String multilingual = "[amFwYW5lc2U6IOaXpeacrOiqniwga29yZWFuOiDsl6zrs7TshLjsmpQsIGFuZCBydXNzaWFuINC30LTRgNCw0LLRgdGC0LLRg9C70YLQtQ==]";

        final Tag t = new Parser(multilingual).parse().get(0);

        final byte[] bytes = byte[].class.cast(t.getValue());
        final String result = new String(bytes, UTF_8);

        assertEquals("japanese: 日本語, korean: 여보세요, and russian здравствулте", result);

        assertEquals(multilingual, SDL.format(result.getBytes(UTF_8)));
        //assertEquals(multilingual, SDL.format(result.getBytes()));

        Tag copy = new Tag("content");
        copy.setValue(new SdlValue<>(bytes, SdlType.BINARY));

        assertEquals(t, copy);


    }

    @Test
    public void testPngImage() throws IOException, SDLParseException {
        final Tag t = new Parser(PNG_IMAGE).parse().get(0);

        final String filename = "target/image.png";

        try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(filename))) {
            final byte[] value = t.getValue(byte[].class);
            out.write(value);
        }

        final byte[] bytes = Files.readAllBytes(Paths.get(filename));

        assertEquals(PNG_IMAGE, SDL.format(bytes));
    }

    @Test
    public void testEquality() {
        final Tag t1 = new Tag("root");
        final Tag c1 = new Tag("content");
        c1.setValue(new SdlValue<>("this should work".getBytes(UTF_8), SdlType.BINARY));
        t1.addChild(c1);

        final Tag t2 = new Tag("root");
        final Tag c2 = new Tag("content");
        c2.setValue(new SdlValue<>("this should work".getBytes(UTF_8), SdlType.BINARY));
        t2.addChild(c2);

        assertEquals(t1, t2);
    }
}
