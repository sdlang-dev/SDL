package com.singingbush.sdl.spring;

import com.singingbush.sdl.Parser;
import com.singingbush.sdl.SDLParseException;
import com.singingbush.sdl.Tag;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SdlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        try {
            final List<Tag> tags = new Parser(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)).parse();

            return name != null ? new SDLangTagSource(name, tags) : new SDLangTagSource("sdl", tags);
        } catch (final SDLParseException e) {
            throw new RuntimeException("Unable to parse SDLang property source", e);
        }
    }
}
