package com.singingbush.sdl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Samael Bate (singingbush)
 * created on 17/05/18
 */
public class SdlValue<T> {

    private final T value;
    private final SdlType type;

    /**
     * Purposely package-private, this constructor is called internally
     * by {@link SDL#value}
     * @param value the Java type for this SDL value
     * @param type the SdlType for this object
     */
    SdlValue(final T value, final SdlType type) {
        this.value=value;
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public SdlType getType() {
        return type;
    }

    /**
     * Create an SDL string representation for an object
     *
     * @return an SDL string representation for an object
     */
    public String getText() {
        switch (type) {
            case STRING:
                return "\"" + escape(String.valueOf(value)) + "\"";
            case STRING_MULTILINE:
                return "`" + String.valueOf(value) + "`";
            case CHARACTER:
                return "'" + escape((Character)value) + "'";
            case BOOLEAN:
                return String.valueOf(value);
            case NUMBER:
                if(value instanceof BigDecimal) {
                    return value.toString() + "BD";
                } else if(value instanceof Float) {
                    return value.toString() + "F";
                } else if(value instanceof Long) {
                    return value.toString() + "L";
                }
                break;
            case DATE:
                return DateTimeFormatter.ofPattern("y/M/d").format(LocalDate.class.cast(value));
            case DATETIME:
                if(LocalDateTime.class.isAssignableFrom(value.getClass())) {
                    return DateTimeFormatter
                        .ofPattern("y/M/d H:m:s.SSS") // .ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
                        .format(LocalDateTime.class.cast(value));
                } else if(ZonedDateTime.class.isAssignableFrom(value.getClass())) {
                    return DateTimeFormatter
                        .ofPattern(SDL.DATE_TIME_FORMAT)
                        //.ofPattern("y/M/d H:m:s.SSS-z")
                        .withLocale(Locale.ENGLISH) // DO NOT REMOVE!! This is needed to force ZoneId to render as short version
                        .format(ZonedDateTime.class.cast(value));
                }
                break;
            case DURATION:
                return timeSpanString(Duration.class.cast(value));
            case BINARY:
                final String data = new String(Base64.getEncoder().encode((byte[])value), StandardCharsets.UTF_8);
                return String.format("[%s]", data);
            case NULL:
                return "null";
        }
        return String.valueOf(value); // shouldn't happen
    }

    private static String timeSpanString(@NotNull final Duration duration) {
        final StringBuilder sb = new StringBuilder();

//        final boolean negative = duration.isNegative();
        long days = duration.toDays();
        final long hours = days == 1L && duration.toHours() == 24L ? 24L : duration.toHours() % 24;
        if(hours == 24L) {
            days = 0L;
        }
        final long minutes = duration.toMinutes() % 60L;
        final long seconds = duration.toMillis() % 60_000L / 1000;
        final long milliseconds = duration.toMillis() % 1_000L;


        if(days!=0) {
            sb.append(days);
            sb.append("d");
            sb.append(":");

            sb.append(padTo2(Math.abs(hours)));
        } else {
            sb.append(padTo2(hours));
        }

        sb.append(":");

        sb.append(padTo2(Math.abs(minutes)));
        sb.append(":");

        sb.append(padTo2(Math.abs(seconds)));

        if(milliseconds!=0) {
            sb.append(".");

            String millis = "" + Math.abs(milliseconds);
            if(millis.length()==1)
                millis="00"+millis;
            else if(millis.length()==2)
                millis="0"+millis;

            sb.append(millis);
        }

        return sb.toString();
    }

    private static String padTo2(long val) {
        if(val>-10 && val<0) {
            return "-0" + Math.abs(val);
        } else if(val>-1 && val<10) {
            return "0" + val;
        }

        return "" + val;
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();

        int size = s.length();
        for(int i=0; i<size; i++) {
            char c = s.charAt(i);
            if(c=='\\')
                sb.append("\\\\");
            else if(c=='"')
                sb.append("\\\"");
            else if(c=='\t')
                sb.append("\\t");
            else if(c=='\r')
                sb.append("\\r");
            else if(c=='\n')
                sb.append("\\n");
            else
                sb.append(c);
        }

        return sb.toString();
    }

    private static String escape(Character ch) {
        char c = ch.charValue();
        switch(c) {
            case '\\': return "\\\\";
            case '\'': return "\\'";
            case '\t': return "\\t";
            case '\r': return "\\r";
            case '\n': return "\\n";
            default: return ""+c;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SdlValue<?> other = (SdlValue<?>) o;

        final boolean sameValue = value != null ? value.getClass().isArray() ? Arrays.equals(
            byte[].class.cast(value),
            byte[].class.cast(other.value)
        ) : (value.getClass().isPrimitive() ? value == other.value : Objects.equals(value, other.value))
            : value == other.value;

        return sameValue && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public String toString() {
        return getText();
    }

}
