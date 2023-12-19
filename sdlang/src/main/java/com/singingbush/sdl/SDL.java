/*
 * Simple Declarative Language (SDL) for Java
 * Copyright 2005 Ikayzo, inc.
 *
 * This program is free software. You can distribute or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by
 * the Free Software Foundation.
 *
 * This program is distributed AS IS and WITHOUT WARRANTY. OF ANY KIND,
 * INCLUDING MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, contact the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.singingbush.sdl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.SortedMap;
import java.util.Base64;

/**
 * Various SDL related utility methods
 *
 * @author Daniel Leuck
 */
public class SDL {

    public static final SdlValue NULL = new SdlValue<>(null, SdlType.NULL);

	/**
	 * <p>The SDL standard date format "yyyy/MM/dd" or "y/M/d"</p>
	 *
	 * <p>Note: SDL uses the Gregorian calendar</p>
	 */
	public static final String DATE_FORMAT = "y/M/d";// "yyyy/MM/dd";

	/**
	 * The SDL standard DATE_TIME format "yyyy/MM/dd HH:mm:ss.SSS-z" or "y/M/d H:m:s.SSS-z"
	 *
	 * <p>Note: SDL uses a 24 hour clock (0-23) and the Gregorian calendar
	 */
	public static final String DATE_TIME_FORMAT = DATE_FORMAT + " H:m:s.SSS-z";// "HH:mm:ss.SSS-z";

	/**
	 * Create an SDL string representation for an object (note: Strings and
	 * Characters will be surrounded by quotes)
	 *
	 * @param object The object to format
	 * @return an SDL string representation for an object
	 */
	public static String format(@Nullable final Object object) {
		return format(object, true);
	}

	/**
	 * Create an SDL string representation for an object
	 *
	 * @param object The object to format
	 * @param escapeText if the object is a String it can be escaped within "" or literal in ``
	 * @return an SDL string representation for an object
	 */
	public static String format(@Nullable final Object object, boolean escapeText) {
	    if(object == null) {
            return "null";
        } else if(object instanceof String) {
			if(escapeText) {
                return "\"" + escape(String.valueOf(object)) + "\"";
            } else {
			    // todo: figure out a good way to end up here
                return "`" + String.valueOf(object) + "`";
            }
		} else if(object instanceof Character) {
            return "'" + escape((Character)object) + "'";
//			if(addQuotes) {
//				return "'" + escape((Character)object) + "'";
//			} else {
//				return escape((Character)object);
//			}
		} else if(object instanceof BigDecimal) {
			return object.toString() + "BD";
		} else if(object instanceof Float) {
			return object.toString() + "F";
		} else if(object instanceof Long) {
			return object.toString() + "L";
		} else if(object instanceof byte[]) {
            final String data = new String(Base64.getEncoder().encode((byte[])object), StandardCharsets.UTF_8);
			return String.format("[%s]", data);
        } else if(LocalDate.class.isAssignableFrom(object.getClass())) {
            return DateTimeFormatter
                .ofPattern(DATE_FORMAT)
                .format(LocalDate.class.cast(object));
        } else if(LocalDateTime.class.isAssignableFrom(object.getClass())) {
            return DateTimeFormatter
                .ofPattern(DATE_TIME_FORMAT)
                .format(LocalDateTime.class.cast(object));
        } else if(ZonedDateTime.class.isAssignableFrom(object.getClass())) {
            return DateTimeFormatter
                .ofPattern(DATE_TIME_FORMAT) // .ofPattern("y/M/d H:m:s.SSS-z")
                .format(ZonedDateTime.class.cast(object));
        } else if(Duration.class.isAssignableFrom(object.getClass())) {
		    return timeSpanString(Duration.class.cast(object));
		}

		return String.valueOf(object);
	}

	private static String timeSpanString(@NotNull final Duration duration) {
        final StringBuilder sb = new StringBuilder();

        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        long seconds = duration.toMillis() / 1000;
        long milliseconds = duration.toMillis();

        if(days!=0) {
            sb.append(days);
            sb.append("d");
            sb.append(":");

            sb.append(padTo2((int)Math.abs(hours)));
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

	/*
	 * <p>Coerce the type to a standard SDL type or throw an illegal argument
	 * exception if no coercion is possible.</p>
	 *
	 * <p>Coercion table</p>
	 * <pre>
	 *     {@code
	 *     String, Character, Integer, Long, Float, Double, BigDecimal,
	 *         Boolean, LocalDateTime, LocalDate, Duration -> No change
	 *     Byte[] -> byte[]
	 *     Byte, Short -> Integer
	 *     Date -> Instant
	 *     }
	 * </pre>
	 *
	 * @param value An object to coerce
	 * @return The value as a Java type, eg: String, Integer
	 * @throws IllegalArgumentException if the type is coercible to a legal SDL
	 *     type
	 */
//	@SuppressWarnings("unchecked")
//	public static SdlValue coerceOrFail(final SdlValue value) {
//		if(value == null)
//			return null;
//
//		final Object obj = value.getValue();
//
//		if(obj instanceof String) {
//            return new SdlValue<>(String.class.cast(obj), SdlType.STRING);
//        } else if(obj instanceof Double) {
//            return new SdlValue<>(Double.class.cast(obj), SdlType.NUMBER);
//        } else if(obj instanceof Integer) {
//            return new SdlValue<>(Integer.class.cast(obj), SdlType.NUMBER);
//        } else if(obj instanceof Boolean) {
//            return new SdlValue<>(Boolean.class.cast(obj), SdlType.BOOLEAN);
//        } else if(obj instanceof BigDecimal) {
//            return new SdlValue<>(BigDecimal.class.cast(obj), SdlType.NUMBER);
//        } else if(obj instanceof Long) {
//            return new SdlValue<>(Long.class.cast(obj), SdlType.NUMBER);
//        } else if(obj instanceof Character) {
//            return new SdlValue<>(Character.class.cast(obj), SdlType.CHARACTER);
//        } else if(obj instanceof Float) {
//            return new SdlValue<>(Float.class.cast(obj), SdlType.NUMBER);
//        } else if(obj instanceof ZonedDateTime) {
//            return new SdlValue<>(ZonedDateTime.class.cast(obj), SdlType.DATETIME);
//        } else if(obj instanceof LocalDateTime) {
//            return new SdlValue<>(LocalDateTime.class.cast(obj), SdlType.DATETIME);
//        } else if(obj instanceof LocalTime) {
//            return new SdlValue<>(LocalTime.class.cast(obj), SdlType.TIME);
//        } else if(obj instanceof LocalDate) {
//            return new SdlValue<>(LocalDate.class.cast(obj), SdlType.DATE);
//        } else if(obj instanceof Duration) {
//			return new SdlValue<>(Duration.class.cast(obj), SdlType.DURATION);
//		}
//
//		Class c = obj.getClass();
//		if(c.isArray()) {
//			Class compType = c.getComponentType();
//
//			if(compType==byte.class)
//				return new SdlValue<>(obj, SdlType.BINARY);
//
//			if(compType==Byte.class) {
//				Byte[] objBytes = (Byte[])obj;
//				byte[] bytes = new byte[objBytes.length];
//				for(int i=0;i<objBytes.length;i++)
//					bytes[i]=objBytes[i];
//
//				return new SdlValue<>(bytes, SdlType.BINARY);
//			}
//		}
//
//		if(obj instanceof Date) {
//			return new SdlValue<>(Date.class.cast(obj).toInstant(), SdlType.DATE);
//		}
//
//		if(obj instanceof Byte || obj instanceof Short) {
//			return new SdlValue<>( ((Number)obj).intValue() , SdlType.NUMBER);
//		}
//
//		throw new IllegalArgumentException(obj.getClass().getName() + " is not coercible to an SDL type");
//	}

	/**
	 * Validates an SDL identifier String.  SDL Identifiers must start with a
	 * Unicode letter or underscore (_) and contain only unicode letters,
	 * digits, underscores (_), and dashes(-).
	 *
	 * @param identifier The identifier to validate
	 * @throws IllegalArgumentException if the identifier is not legal
	 */
	public static void validateIdentifier(@NotNull final String identifier) {
		if(identifier==null || identifier.length()==0) {
            throw new IllegalArgumentException("SDL identifiers cannot be null or empty.");
        }

		if(!Character.isJavaIdentifierStart(identifier.charAt(0)))
			throw new IllegalArgumentException("'" + identifier.charAt(0) +
					"' is not a legal first character for an SDL identifier. " +
					"SDL Identifiers must start with a unicode letter or " +
					"an underscore (_).");

		int identifierSize=identifier.length();
		for(int i=1; i<identifierSize; i++) {
			final char c = identifier.charAt(i);
			if(!String.valueOf(c).matches("(\\w|\\n|-|_|\\.|\\$)")) {
				throw new IllegalArgumentException("'" + c +
						"' is not a legal character for an SDL identifier. " +
						"SDL Identifiers must start with a unicode letter or " +
						"underscore (_) followed by 0 or more unicode " +
						"letters, digits, underscores (_), or dashes (-)");
			}
		}
	}

    /**
     * Constructs a new tag using the default name "content"
     * @return a {@link TagBuilder} which can be used to easily create a {@link Tag}
     * @since 2.1.1
     */
	public static TagBuilder tag() {
	    return tag("content");
    }

    /**
     * @param name must be a legal SDL identifier (see {@link SDL#validateIdentifier(String)})
     * @return a {@link TagBuilder} which can be used to easily create a {@link Tag}
     * @since 2.1.0
     */
	public static TagBuilder tag(@NotNull final String name) {
	    return new TagBuilder(name);
    }

    /**
     * @param value text to be converted to SDL
     * @param literal in SDLang multiline strings are supported
     * @return an SDL string
     * @since 2.1.0
     */
    public static SdlValue<String> value(@NotNull final String value, final boolean literal) {
        return literal?
            new SdlValue<>(Parser.parseMultilineString(String.format("`%s`", value)), SdlType.STRING_MULTILINE) :
            new SdlValue<>(Parser.parseString(String.format("\"%s\"", value)), SdlType.STRING);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL char
     * @since 2.1.0
     */
    public static SdlValue<Character> value(final char value) {
        return new SdlValue<>(value, SdlType.CHARACTER);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL boolean
     * @since 2.1.0
     */
    public static SdlValue<Boolean> value(final boolean value) {
        return new SdlValue<>(value, SdlType.BOOLEAN);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL long
     * @since 2.1.0
     */
    public static SdlValue<Long> value(final long value) {
        return new SdlValue<>(value, SdlType.NUMBER);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL float
     * @since 2.1.0
     */
    public static SdlValue<Float> value(final float value) {
        return new SdlValue<>(value, SdlType.NUMBER);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL double
     * @since 2.1.0
     */
    public static SdlValue<Double> value(final double value) {
        return new SdlValue<>(value, SdlType.NUMBER);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL integer
     * @since 2.1.0
     */
    public static SdlValue<Integer> value(final int value) {
        return new SdlValue<>(value, SdlType.NUMBER);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL date
     * @since 2.1.0
     */
    public static SdlValue<LocalDate> value(@NotNull final LocalDate value) {
        return new SdlValue<>(value, SdlType.DATE);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL datetime without timezone
     * @since 2.1.0
     */
    public static SdlValue<LocalDateTime> value(@NotNull final LocalDateTime value) {
        return new SdlValue<>(value, SdlType.DATETIME);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL datetime with timezone
     * @since 2.1.0
     */
    public static SdlValue<ZonedDateTime> value(@NotNull final ZonedDateTime value) {
        return new SdlValue<>(value, SdlType.DATETIME);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL timespan
     * @since 2.1.0
     */
    public static SdlValue<Duration> value(@NotNull final Duration value) {
        return new SdlValue<>(value, SdlType.DURATION);
    }

    /**
     * @param value text to be converted to SDL
     * @return an SDL binary
     * @since 2.1.0
     */
    public static SdlValue value(final byte[] value) {
        return new SdlValue<>(value, SdlType.BINARY);
    }

	/**
	 * Get the value represented by a string containing an SDL literal.
	 *
	 * @param literal The literal string to parse
	 * @return A value for an SDL literal
	 * @throws IllegalArgumentException If the text is null or the text does not
	 *         represent a valid SDL literal
	 * @throws NumberFormatException If the text represents a malformed number.
	 */
	@Deprecated
	public static SdlValue value(String literal) {
		if(literal==null) {
            throw new IllegalArgumentException("literal argument to SDL.value(String) cannot be null");
        }

		if(literal.startsWith("\""))
			return new SdlValue<>(Parser.parseString(literal), SdlType.STRING);
        if(literal.startsWith("`"))
            return new SdlValue<>(Parser.parseMultilineString(literal), SdlType.STRING_MULTILINE);
		if(literal.startsWith("'"))
			return new SdlValue<>(Parser.parseCharacter(literal), SdlType.CHARACTER);
		if(literal.equals("null"))
			return new SdlValue<>(null, SdlType.NULL);
		if(literal.equals("true") || literal.equals("on"))
			return new SdlValue<>(Boolean.TRUE, SdlType.BOOLEAN);
		if(literal.equals("false") || literal.equals("off"))
			return new SdlValue<>(Boolean.FALSE, SdlType.BOOLEAN);
		if(literal.startsWith("["))
			return new SdlValue<>(Parser.parseBinary(literal), SdlType.BINARY);
		if(literal.matches("\\d+[-|/]\\d+[-|/]\\d+")) { //literal.matches("\\d+/\\d+/\\d+")) {
            return new SdlValue<>(Parser.parseDate(literal), SdlType.DATE);
        }
        if(literal.matches(Parser.DATETIME_REGEX)) { // "(\\d+/\\d+/\\d+) ((\\d+:\\d+:\\d+(.\\d+)?)(-\\w+)?)"
		    return literal.contains("-") ?
                new SdlValue<>(Parser.parseZonedDateTime(literal), SdlType.DATETIME) :
                new SdlValue<>(Parser.parseLocalDateTime(literal), SdlType.DATETIME);
        }

		if(literal.matches(Parser.TIMESPAN_REGEX)) { // "-?(\\d+d:)?(\\d+:\\d+:\\d+)(.\\d+)?"
            return new SdlValue<>(Parser.parseTimeSpan(literal), SdlType.DURATION);
        }
		if("01234567890-.".indexOf(literal.charAt(0)) != -1) {
            return new SdlValue<>(Parser.parseNumber(literal), SdlType.NUMBER);
        }

		throw new IllegalArgumentException("String " + literal + " does not represent an SDL type.");
	}

	/**
	 * <p>Parse the string of values and return a list.  The string is handled
	 * as if it is the values portion of an SDL tag.</p>
	 *
	 * Example
	 * <pre>
	 *     List list = SDL.list("1 true 12:24:01");
	 * </pre>
	 *
	 * <p>Will return an int, a boolean, and a time span.</p>
	 *
	 * Note: If you want the more descriptive SDLParseException to be thrown use:
	 * <pre>
	 *     List list = new Tag("root").read("1 true 12:24:01")
	 *         .getChild("content").getValues();
	 * </pre>
	 *
	 * @param valueList A string of space separated SDL literals
	 * @return A list of values
	 * @throws IllegalArgumentException If the string is null or contains
	 *     literals that cannot be parsed
	 */
	public static List list(@NotNull final String valueList) {
		if(valueList==null) {
            throw new IllegalArgumentException("valueList argument to SDL.list(String) cannot be null");
        }

		try {
			return new Tag("root").read(valueList).getChild("content").getValues();
		} catch(SDLParseException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * <p>Parse a string representing the attributes portion of an SDL tag
	 * and return the results as a map.</p>
	 *
	 * Example
	 * <pre>
	 * {@code
	 *     Map<String,Object> m = SDL.map("value=1 debugging=on time=12:24:01");
	 * }
	 * </pre>
	 *
	 * <p>Will return a map containing value=1, debugging=true, and
	 * time=12:24:01</p>
	 *
	 * Note: If you want the more descriptive SDLParseException to be thrown use:
	 * <pre>
	 * {@code
	 *     Map<String,Object> m = new Tag("root")
	 *         .read("atts " + attributeString).getChild("atts")
	 *         .getAttributes();
	 * }
	 * </pre>
	 *
	 * @param attributeString A string of space separated key=value pairs
	 * @return A map created from the attribute string
	 * @throws IllegalArgumentException If the string is null or contains
	 *     literals that cannot be parsed or the map is malformed
	 */
	public static SortedMap<String,SdlValue> map(@NotNull final String attributeString) {
		if(attributeString==null) {
            throw new IllegalArgumentException("attributeString argument to SDL.map(String) cannot be null");
        }

		try {
			return new Tag("root")
                .read("atts " + attributeString)
                .getChild("atts")
                .getAttributes();
		} catch(final SDLParseException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}
