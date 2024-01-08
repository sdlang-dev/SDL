package com.singingbush.sdl;

import com.singingbush.sdl.annotations.Attribute;
import com.singingbush.sdl.annotations.Value;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;

/*
* Similar to com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
*
*/
public class SdlAnnotationProcessor {

    private final Object obj;

    public SdlAnnotationProcessor(Object obj) {
        this.obj = obj;
        if(!obj.getClass().isAnnotationPresent(com.singingbush.sdl.annotations.Tag.class)) {
            throw new IllegalArgumentException(obj.getClass().getSimpleName() + " does not have @Tag annotation");
        }
    }

    public Tag process() {
        final com.singingbush.sdl.annotations.Tag tagAnn = obj.getClass().getAnnotation(com.singingbush.sdl.annotations.Tag.class);

        final TagBuilder tagBuilder = SDL.tag(tagAnn.value()).withNamespace(tagAnn.namespace());

        Arrays.stream(obj.getClass().getDeclaredFields())
            .forEach(field -> {

                if(field.isAnnotationPresent(Value.class)) {
                    processValueField(tagBuilder, field);
                }

                if(field.isAnnotationPresent(Attribute.class)) {
                    processAttributeField(tagBuilder, field);
                }
            });


        return tagBuilder.build();
    }

    private void processValueField(final TagBuilder tagBuilder, final Field field) {
        field.setAccessible(true);
        //final Value valAnn = field.getAnnotation(Value.class);

        try {
            // SDL only supports a limited amount of literal types:
            // STRING, STRING_MULTILINE, CHARACTER, BOOLEAN, NUMBER, DATE, DATETIME, DURATION, BINARY, NULL
            // which are supported via the following Java types:
            // String, Character, Boolean, Long, Float, Double, Integer, LocalDate, LocalDateTime, ZonedDateTime, Duration
            // todo: handle objects that are also pojos annotated with @Tag
            switch (field.getType().getSimpleName()) {
                case "String":
                    tagBuilder.withValue(SDL.value(String.valueOf(field.get(obj)), false));
                    break;
                case "Character":
                    tagBuilder.withValue(SDL.value(Character.class.cast(field.get(obj))));
                    break;
                case "Boolean":
                    tagBuilder.withValue(SDL.value(Boolean.class.cast(field.get(obj))));
                    break;
                case "Long":
                    tagBuilder.withValue(SDL.value(Long.class.cast(field.get(obj))));
                    break;
                case "Float":
                    tagBuilder.withValue(SDL.value(Float.class.cast(field.get(obj))));
                    break;
                case "Double":
                    tagBuilder.withValue(SDL.value(Double.class.cast(field.get(obj))));
                    break;
                case "Integer":
                    tagBuilder.withValue(SDL.value(Integer.class.cast(field.get(obj))));
                    break;
                case "LocalDate":
                    tagBuilder.withValue(SDL.value(LocalDate.class.cast(field.get(obj))));
                    break;
                case "LocalDateTime":
                    tagBuilder.withValue(SDL.value(LocalDateTime.class.cast(field.get(obj))));
                    break;
                case "ZonedDateTime":
                    tagBuilder.withValue(SDL.value(ZonedDateTime.class.cast(field.get(obj))));
                    break;
                case "Duration":
                    tagBuilder.withValue(SDL.value(Duration.class.cast(field.get(obj))));
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // todo: use slf4j-api to log error, also perhaps throw an Exception??
        }
    }

    private void processAttributeField(final TagBuilder tagBuilder, final Field field) {
        field.setAccessible(true);
        final Attribute attrAnn = field.getAnnotation(Attribute.class);
        final String name = !attrAnn.value().isEmpty() ? attrAnn.value() : field.getName();

        try {
            switch (field.getType().getSimpleName()) {
                case "String":
                    tagBuilder.withAttribute(name, SDL.value(String.valueOf(field.get(obj)), false));
                    break;
                case "Character":
                    tagBuilder.withAttribute(name, SDL.value(Character.class.cast(field.get(obj))));
                    break;
                case "Boolean":
                    tagBuilder.withAttribute(name, SDL.value(Boolean.class.cast(field.get(obj))));
                    break;
                case "Long":
                    tagBuilder.withAttribute(name, SDL.value(Long.class.cast(field.get(obj))));
                    break;
                case "Float":
                    tagBuilder.withAttribute(name, SDL.value(Float.class.cast(field.get(obj))));
                    break;
                case "Double":
                    tagBuilder.withAttribute(name, SDL.value(Double.class.cast(field.get(obj))));
                    break;
                case "Integer":
                    tagBuilder.withAttribute(name, SDL.value(Integer.class.cast(field.get(obj))));
                    break;
                case "LocalDate":
                    tagBuilder.withAttribute(name, SDL.value(LocalDate.class.cast(field.get(obj))));
                    break;
                case "LocalDateTime":
                    tagBuilder.withAttribute(name, SDL.value(LocalDateTime.class.cast(field.get(obj))));
                    break;
                case "ZonedDateTime":
                    tagBuilder.withAttribute(name, SDL.value(ZonedDateTime.class.cast(field.get(obj))));
                    break;
                case "Duration":
                    tagBuilder.withAttribute(name, SDL.value(Duration.class.cast(field.get(obj))));
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // todo: use slf4j-api to log error, also perhaps throw an Exception??
        }
    }
}
