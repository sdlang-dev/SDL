package com.singingbush.sdl.spring;

import com.singingbush.sdl.Tag;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SDLangTagSource extends PropertySource<List<Tag>> {

    private final Pattern indexedPattern;

    public SDLangTagSource(final @NonNull String name, final @NonNull List<Tag> source) {
        super(name, source);

        this.indexedPattern = Pattern.compile("(\\S+)\\[(\\d+)]");
    }


    /*
    * It may make more sense to return a Tag and find way for Spring to know how to convert them to required type
    */
    @Override
    @Nullable
    public Object getProperty(String valueExpression) {
        if (ObjectUtils.isEmpty(valueExpression)) {
            throw new IllegalArgumentException("arg cannot be empty");
        }

        super.logger.debug(String.format("Retrieving SDL tag using : \"%s\"", valueExpression));

        // the value expression could be a name for an SDL tag or a name with attribute, eg: "person|age"
        final String[] parts = valueExpression.split("\\|", 2);

        if (Arrays.stream(parts).anyMatch(ObjectUtils::isEmpty)) {
            throw new IllegalArgumentException("Invalid expression");
        }

        // find the Tag by name
        final Optional<Tag> result = getSDLangTagByName(parts[0], getSource());

        return result
            .flatMap(tag -> {
                // get value or attribute
                final List<Object> values = tag.getValues();
                final Object sdlValue = values.size() == 1 ? values.get(0) : values;
                final Object valOrAttr = parts.length > 1 ? tag.getAttribute(parts[1]) : sdlValue;
                return Optional.ofNullable(valOrAttr);
            })
            .orElse(null);
    }

    public Optional<Tag> getSDLangTagByName(final @NonNull String name, final @NonNull List<Tag> tags) {
        if (tags.isEmpty()) {
            return Optional.empty();
        }

        // to support nested nodes we split on the first '.' and if there's a 2nd String call this method again recursively
        final String[] parts = name.split("\\.", 2);

        final Matcher matcher = this.indexedPattern.matcher(parts[0]);

        Optional<Tag> tag;

        if (matcher.find()) {
            // we are dealing with an indexed node
            final String nme = matcher.group(1);
            final int index = Integer.parseInt(matcher.group(2));

//            super.logger.debug(String.format("Getting %s at index %s", nme, index));

            tag = Optional.ofNullable(tags.stream()
                .filter(t -> t.getName().equals(nme))
                .collect(Collectors.toList())
                .get(index));
        } else {
            tag = tags.stream()
                .filter(t -> t.getName().equals(parts[0]))
                .distinct()
                .findFirst();
        }

        return parts.length > 1 ?
            getSDLangTagByName(parts[1], tag.isPresent() ? tag.get().getChildren() : Collections.emptyList()) :
            tag
            ;
    }

}
