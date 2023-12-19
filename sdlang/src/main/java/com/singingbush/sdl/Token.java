package com.singingbush.sdl;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An SDL token.
 *
 * @author Daniel Leuck
 */
class Token {

    private static final String DATE_REGEX = "(\\d+\\/\\d+\\/\\d+)";
    private static final String TIME_REGEX = "(\\d+:\\d+(:\\d+)?(.\\d+)?)(-\\w+)?";
    private static final String DATETIME_REGEX = DATE_REGEX + " " + TIME_REGEX;
    private static final String TIMESPAN_REGEX = "-?(\\d+d:)?(\\d+:\\d+:\\d+)(.\\d+)?";

    private SdlType type;
    private final String text;
    private final int line;
    private final int position;
    private final int size;

    private SdlValue sdlValue;

    private final boolean punctuation;
    private final boolean literal;

    Token(String text, int line, int position) throws SDLParseException {
        this.text=text;

        this.line=line;
        this.position=position;
        size=text.length();

        try {
            if(text.startsWith("\"")) {
                type = SdlType.STRING;
                sdlValue = new SdlValue<>(Parser.parseString(text), SdlType.STRING);
            } else if(text.startsWith("`")) {
                type = SdlType.STRING_MULTILINE;
                sdlValue = new SdlValue<>(Parser.parseMultilineString(text), SdlType.STRING_MULTILINE);
            } else if(text.startsWith("'")) {
                type = SdlType.CHARACTER;
                sdlValue = new SdlValue<>(text.charAt(1), SdlType.CHARACTER);
            } else if(text.equals("null")) {
                type = SdlType.NULL;
                sdlValue = new SdlValue<>(null, SdlType.NULL);
            } else if(text.equals("true") || text.equals("on")) {
                type = SdlType.BOOLEAN;
                sdlValue = new SdlValue<>(true, SdlType.BOOLEAN);
            } else if(text.equals("false") || text.equals("off")) {
                type = SdlType.BOOLEAN;
                sdlValue = new SdlValue<>(false, SdlType.BOOLEAN);
            } else if(text.startsWith("[")) {
                type = SdlType.BINARY;
                sdlValue = new SdlValue<>(Parser.parseBinary(text), SdlType.BINARY);
            } else if(text.matches(DATE_REGEX)) {
                type = SdlType.DATE;
                sdlValue = new SdlValue<>(Parser.parseDate(text), SdlType.DATE);
            } else if(text.matches(DATETIME_REGEX)) {
                type = SdlType.DATETIME;
                final Object dt = text.contains("-") ? Parser.parseZonedDateTime(text) : Parser.parseLocalDateTime(text);
                sdlValue = new SdlValue<>(dt, SdlType.DATETIME);
            } else if(text.matches(TIMESPAN_REGEX)) {
                type = SdlType.DURATION;
                sdlValue = new SdlValue<>(Parser.parseTimeSpan(text), SdlType.DURATION);
            } else if("01234567890-.".indexOf(text.charAt(0))!=-1) {
                type = SdlType.NUMBER;
                sdlValue = new SdlValue<>(Parser.parseNumber(text), SdlType.NUMBER);
            } else {
                char c = text.charAt(0);
                switch(c) {
                    case '{': type = SdlType.START_BLOCK; break;
                    case '}': type = SdlType.END_BLOCK; break;
                    case '=': type = SdlType.EQUALS; break;
                    case ':': type = SdlType.COLON; break;
                    case ';': type = SdlType.SEMICOLON; break;
                }
            }
        } catch(final IllegalArgumentException iae) {
            throw new SDLParseException(iae.getMessage(), line, position);
        }

        if(type == null) {
            type = SdlType.IDENTIFIER;
        }

        punctuation = type==SdlType.COLON || type==SdlType.EQUALS ||
            type==SdlType.START_BLOCK || type==SdlType.END_BLOCK;

        literal =  type!=SdlType.IDENTIFIER && !punctuation;
    }

    public SdlType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    public int getSize() {
        return size;
    }

    public boolean isPunctuation() {
        return punctuation;
    }

    public boolean isLiteral() {
        return literal;
    }

    /**
     * @return SdlValue
     * @deprecated As of release 2.0.0, replaced by {@link #getSdlValue()}
     */
    @Deprecated
    SdlValue getObjectForLiteral() {
        return sdlValue;
    }

    /**
     * @return Object
     * @deprecated As of release 2.0.0, replaced by {@link #getJavaObject()}
     */
    @Deprecated
    public Object getObject() {
        return sdlValue;
    }

    /**
     *
     * @return the SdlValue for this Token
     * @since 2.0.0
     */
    public SdlValue getSdlValue() {
        return sdlValue;
    }

    /**
     *
     * @return the Object for this Token
     * @since 2.0.0
     */
    @Nullable
    public Object getJavaObject() {
        return sdlValue != null ? sdlValue.getValue() : null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return line == token.line &&
            position == token.position &&
            size == token.size &&
            punctuation == token.punctuation &&
            literal == token.literal &&
            type == token.type &&
            Objects.equals(text, token.text) &&
            Objects.equals(sdlValue, token.sdlValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text, line, position, size, sdlValue, punctuation, literal);
    }

    @Override
    public String toString() {
        return type + " " + text + " pos:" + position;
    }

}
