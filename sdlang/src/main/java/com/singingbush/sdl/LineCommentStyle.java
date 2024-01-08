package com.singingbush.sdl;

/**
 * SDLang supports three styles of line comments
 */
public enum LineCommentStyle {
    CPP("//"),
    SHELL("#"),
    LUA("--");

    private final String value;

    LineCommentStyle(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
