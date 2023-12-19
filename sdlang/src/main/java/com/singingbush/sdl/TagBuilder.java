package com.singingbush.sdl;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Samael Bate (singingbush)
 * created on 20/07/18
 * @since 2.1.0
 */
public class TagBuilder {

    private final String name;
    private String namespace;
    private String comment;
    private List<SdlValue> values = new ArrayList<>();
    private List<Tag> children = new ArrayList<>();
    private Map<String, SdlValue> attributes = new HashMap<>();

    /**
     * @param name must be a legal SDL identifier (see {@link SDL#validateIdentifier(String)})
     * @since 2.1.0
     */
    TagBuilder(@NotNull final String name) {
        this.name = name;
    }

    /**
     * In SDL you can optionally specify a namespace
     * @param name must be a legal SDL identifier (see {@link SDL#validateIdentifier(String)})
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withNamespace(@NotNull final String name) {
        this.namespace = name;
        return this;
    }

    /**
     *
     * @param comment a single line of text that will precede the tag when serialised
     * @return this TagBuilder
     * @since 2.1.0
     */
    public TagBuilder withComment(@NotNull final String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * In SDL you can optionally have one or more values
     * @param value an SDL object, see {@link SdlValue}
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withValue(@NotNull final SdlValue value) {
        this.values.add(value);
        return this;
    }

    /**
     * In SDL you can optionally have one or more values
     * @param values multiple SDL objects, see {@link SdlValue}
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withValues(@NotNull final SdlValue... values) {
        this.values.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * In SDL you can optionally have one or more values
     * @param values multiple SDL objects, see {@link SdlValue}
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withValues(@NotNull final List<SdlValue> values) {
        this.values.addAll(values);
        return this;
    }

    /**
     * In SDL you can optionally have one or more children
     * @param child a child {@link Tag} object
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withChild(@NotNull final Tag child) {
        this.children.add(child);
        return this;
    }

    /**
     * In SDL you can optionally have one or more children
     * @param children multiple child {@link Tag} objects
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withChildren(@NotNull final Tag... children) {
        this.children.addAll(Arrays.asList(children));
        return this;
    }

    /**
     * In SDL you can optionally have one or more children
     * @param children multiple child {@link Tag} objects
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withChildren(@NotNull final List<Tag> children) {
        this.children.addAll(children);
        return this;
    }

    /**
     * In SDL you can optionally have one or more attributes
     * @param key attribute key
     * @param value attribute value
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withAttribute(@NotNull final String key, @NotNull final SdlValue value) {
        this.attributes.put(key, value);
        return this;
    }

    /**
     * In SDL you can optionally have one or more attributes
     * @param attributes multiple attributes
     * @return this TagBuilder
     * @since 2.1.0
     */
    @NotNull
    public TagBuilder withAttributes(@NotNull final Map<String, SdlValue> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    /**
     * @return a newly created {@link Tag} object
     * @throws IllegalArgumentException if the name is not a legal SDL
     *     identifier (see {@link SDL#validateIdentifier(String)}) or the
     *     namespace is non-blank and is not a legal SDL identifier.
     */
    @NotNull
    public Tag build() {
        final Tag t = namespace != null? new Tag(namespace, name) : new Tag(name);
        t.setComment(comment);
        values.forEach(t::addValue);
        children.forEach(t::addChild);
        t.setAttributes(attributes); // attributes.forEach(t::setAttribute);
        return t;
    }

}
