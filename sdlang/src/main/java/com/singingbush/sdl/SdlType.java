package com.singingbush.sdl;

public enum SdlType {
    IDENTIFIER,

    // punctuation
    COLON, SEMICOLON, EQUALS, START_BLOCK, END_BLOCK,

    // literals
    STRING, STRING_MULTILINE, CHARACTER, BOOLEAN, NUMBER, DATE, DATETIME, DURATION, BINARY, NULL
}
