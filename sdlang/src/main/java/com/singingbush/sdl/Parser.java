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

import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The SDL parser.
 *
 * @author Daniel Leuck
 */
public class Parser {

    private static final String DATE_REGEX = "(\\d+\\/\\d+\\/\\d+)";
    private static final String TIME_REGEX = "(\\d+:\\d+(:\\d+)?(.\\d+)?)(-\\w+)?";
    static final String DATETIME_REGEX = DATE_REGEX + " " + TIME_REGEX;
    static final String TIMESPAN_REGEX = "-?(\\d+d:)?(\\d+:\\d+:\\d+)(.\\d+)?";

    private final BufferedReader reader;
	private String line;
	private List<Token> toks;
	private StringBuilder sb;
	private boolean startEscapedQuoteLine;
	private int lineNumber=-1, lineStart = 0, pos=0, lineLength=0, tokenStart=0;
    private boolean semicolonTerminated = false;

	/**
	 * Create an SDL parser
     * @param reader A Reader for the SDL that should be parsed
	 */
	public Parser(@NotNull Reader reader) {
		this.reader = (reader instanceof BufferedReader)
			? ((BufferedReader)reader)
			: new BufferedReader(reader);
	}

	/**
	 * Convenience for users wanting to parse SDL syntax that is held in a Java String
	 * @param sdlText a string of SDLang
	 * @since 1.4.0
	 */
	public Parser(@NotNull final String sdlText) {
		this(new StringReader(new String(sdlText.getBytes(), UTF_8)));
		//this(new InputStreamReader(new ByteArrayInputStream(sdlText.getBytes())));
	}

	/**
	 * Convenience for users wanting to parse SDL from a java.io.File
	 * @param file A UTF-8 encoded .sdl file
     * @throws FileNotFoundException If file cannot be found
	 * @since 1.4.0
	 */
	public Parser(@NotNull final File file) throws FileNotFoundException {
		this(new InputStreamReader(new FileInputStream(file), UTF_8));
	}

	/**
	 * @return A list of tags described by the input
	 * @throws IOException If a problem is encountered with the reader
	 * @throws SDLParseException If the document is malformed
	 */
    public List<Tag> parse() throws IOException, SDLParseException {
		final List<Tag> tags = new ArrayList<>();
		List<Token> toks;

		while((toks=getLineTokens()) != null) {
			int size = toks.size();

			if(toks.get(size-1).getType()==SdlType.START_BLOCK) {
				Tag t = constructTag(toks.subList(0, size-1));
				addChildren(t);
				tags.add(t);
			} else if(toks.get(0).getType()==SdlType.END_BLOCK){
				parseException("No opening block ({) for close block (}).", toks.get(0).getLine(), toks.get(0).getPosition());
			} else {
				List<Token> tokens = new ArrayList<>(size);
				for (final Token t : toks) {
					tokens.add(t);
					if(SdlType.SEMICOLON.equals(t.getType())) {
						tags.add(constructTag(tokens));
						tokens = new ArrayList<>(size);
					}
				}
				if(!tokens.isEmpty()) {
					tags.add(constructTag(tokens));
				}
			}
		}

		reader.close();

		return tags;
	}

	private void addChildren(Tag parent) throws SDLParseException, IOException {
		List<Token> toks;
		while((toks=getLineTokens())!=null) {
			int size = toks.size();

			if(toks.get(0).getType()==SdlType.END_BLOCK) {
				return;
			} else if(toks.get(size-1).getType()==SdlType.START_BLOCK) {
				Tag tag = constructTag(toks.subList(0, size-1));
				addChildren(tag);
				parent.addChild(tag);
			} else {
				parent.addChild(constructTag(toks));
			}
		}

		// we have to use -2 for position rather than -1 for unknown because
		// the parseException method adds 1 to line and position
		parseException("No close block (}).", lineNumber, -2);
	}

	/**
	 * Construct a tag (but not its children) from a string of tokens
	 *
	 * @throws SDLParseException
	 */
	Tag constructTag(List<Token> toks) throws SDLParseException {
	    if(lineNumber ==141) {
	        int sdf=23;
        }
		if(toks.isEmpty())
			// we have to use -2 for position rather than -1 for unknown because
			// the parseException method adds 1 to line and position
			parseException("Internal Error: Empty token list", lineNumber, -2);

		Token t0 = toks.get(0);


		if(t0.isLiteral()) {
			toks.add(0, t0 = new Token("content", -1, -1));
		} else if(!SdlType.IDENTIFIER.equals(t0.getType())) {
			expectingButGot("IDENTIFIER", "" + t0.getType() + " (" + t0.getText() + ")",
					t0.getLine(), t0.getPosition());
		}

		int size = toks.size();

		Tag tag = null;

		if(size == 1) {
			tag = new Tag(t0.getText());
		} else {
			int valuesStartIndex = 1;

			final Token t1 = toks.get(1);

			if(SdlType.COLON.equals(t1.getType())) {
				if(size==2 || !SdlType.IDENTIFIER.equals(toks.get(2).getType())) {
                    parseException("Colon (:) encountered in unexpected location.", t1.getLine(), t1.getPosition());
                }

				Token t2 = toks.get(2);
				tag = new Tag(t0.getText(), t2.getText());

				valuesStartIndex = 3;
			} else {
				tag = new Tag(t0.getText());
			}

			// read values
			int i = addTagValues(tag, toks, valuesStartIndex);

			// read attributes
			if(i<size) {
                addTagAttributes(tag, toks, i);
            }
		}

		return tag;
	}

	/**
	 * @return The position at the end of the value list
	 */
	private int addTagValues(Tag tag, List<Token> toks, int tpos) throws SDLParseException {

		int size=toks.size(), i=tpos;

		for(; i < size; i++) {
			Token t = toks.get(i);
			if(t.isLiteral()) {

//			    if(SdlType.DATETIME.equals(t.getType())) { // don't think I need this
//                    tag.addValue( new SdlValue<>(ZonedDateTime.class.cast(t.getObjectForLiteral().getValue()), SdlType.DATETIME) );
////                    tag.addValue(LocalDateTime.class.cast(t.getObjectForLiteral()));
//                }


                tag.addValue(t.getObjectForLiteral());

			} else if(SdlType.IDENTIFIER.equals(t.getType())) {
                break;

			    // todo: CHECK THIS CODE - SOMETHING HERE IS WRONG/BROKEN
//			    if( (i+2) < size && SdlType.EQUALS.equals(toks.get(i+1).getType()) ) {
//                    tag.setAttribute(t.getText(), toks.get(i+2).getObjectForLiteral());
//                    i++;//i +=2;
//                } else {
//                    break;
//                }
                //i++;//i +=2;

			} else {
				expectingButGot("LITERAL or IDENTIFIER", t.getType(), t.getLine(), t.getPosition());
			}
		}

		return i;
	}

	/**
	 * Add attributes to the given tag
	 */
	private void addTagAttributes(final Tag tag, final List<Token> toks, final int tpos) throws SDLParseException {

		int i = tpos;
        final int size = toks.size();

        while(i<size) {
			Token t = toks.get(i);
			if(t.getType()!=SdlType.IDENTIFIER) {
                expectingButGot("IDENTIFIER", t.getType(), t.getLine(), t.getPosition());
            }
			final String nameOrNamespace = t.getText();

			if(i == size -1) {
                expectingButGot("\":\" or \"=\" \"LITERAL\"", "END OF LINE.", t.getLine(), t.getPosition());
            }

			t = toks.get(++i);
			if(t.getType()==SdlType.COLON) {
				if(i==size-1) {
                    expectingButGot("IDENTIFIER", "END OF LINE", t.getLine(), t.getPosition());
                }

				t = toks.get(++i);
				if(t.getType()!=SdlType.IDENTIFIER) {
                    expectingButGot("IDENTIFIER", t.getType(), t.getLine(), t.getPosition());
                }
				String name = t.getText();

				if(i==size-1) {
                    expectingButGot("\"=\"", "END OF LINE", t.getLine(), t.getPosition());
                }
				t = toks.get(++i);
				if(t.getType()!=SdlType.EQUALS) {
                    expectingButGot("\"=\"", t.getType(), t.getLine(), t.getPosition());
                }

				if(i==size-1) {
                    expectingButGot("LITERAL", "END OF LINE", t.getLine(), t.getPosition());
                }
				t = toks.get(++i);
				if(!t.isLiteral()) {
                    expectingButGot("LITERAL", t.getType(), t.getLine(), t.getPosition());
                }

                if(nameOrNamespace.equals(name)) {
                    tag.setAttribute(name, t.getSdlValue());
                } else {
                    tag.setAttribute(nameOrNamespace, name, t.getSdlValue());
                }

			} else if(SdlType.EQUALS.equals(t.getType())) {
				if(i==size-1) {
                    expectingButGot("LITERAL", "END OF LINE", t.getLine(), t.getPosition());
                }

                t = toks.get(++i);

				if(!t.isLiteral()) {
                    expectingButGot("LITERAL", t.getType(), t.getLine(), t.getPosition());
                }

//				if(SdlType.DATETIME.equals(t.getType())) {
//				    tag.setAttribute(nameOrNamespace, t.getObjectForLiteral());
//                }
                tag.setAttribute(nameOrNamespace, t.getSdlValue());

			} else {
				expectingButGot("\":\" or \"=\"", t.getType(), t.getLine(), t.getPosition());
			}

			i++;
		}
	}

	/**
	 * Get a line as tokens.  This method handles line continuations both
	 * within and outside String literals.
	 *
	 * @return A logical line as a list of Tokens
	 * @throws SDLParseException If the SDL input is malformed
	 * @throws IOException If there is an IO problem reading the source
	 */
	@Nullable
	List<Token> getLineTokens() throws SDLParseException, IOException {
//		line = readLine();
        if(semicolonTerminated) {
            semicolonTerminated = false;
        } else {
            line = readLine();
        }

		if(line==null) {
            return null;
        }
		toks = new ArrayList<>();
		lineLength = line.length();
		sb = null;
		tokenStart=0;

		for(;pos<lineLength; pos++) {
			char c=line.charAt(pos);

            completeToken();

			if(c=='"') {
				// handle "" style strings including line continuations
				handleDoubleQuoteString();
			} else if(c=='\'') {
				// handle character literals
				handleCharacterLiteral();
			} else if("{}=:".indexOf(c)!=-1) {
				// handle punctuation
				toks.add(new Token(""+c, lineNumber, pos));
				sb=null;
			} else if(c=='#') {
				// handle hash comments
				break;
			} else if(c=='/') {
				// handle /**/ and // style comments

				if((pos+1)<lineLength && line.charAt(pos+1)=='/')
					break;
				else
					handleSlashComment();
			} else if(c=='`') {
				// handle multiline `` style strings
				handleLiteralString();
			} else if(c=='[') {
				// handle binary literals

				handleBinaryLiteral();

			} else if(c==' ' || c=='\t') {
				// eat whitespace
				while((pos+1)<lineLength && " \t".indexOf(line.charAt(pos+1))!=-1) {
					pos++;
				}
			} else if(c=='\\') {
				// line continuations (outside a string literal)

				// backslash line continuation outside of a String literal
				// can only occur at the end of a line
				handleLineContinuation();
			} else if("0123456789-.".indexOf(c) != -1) {
				if(c=='-' && (pos+1) < lineLength && line.charAt(pos+1)=='-') {
					break;
				}
				// handle numbers, dates, and time spans
				handleNumberDateOrTimeSpan();
			} else if( String.valueOf(c).matches("(\\w|\\n|-|_|\\.|\\$)") ) {
				// handle identifiers
				handleIdentifier();
			} else if(c==';') {
                if(toks.size()==0) {
                    continue;
                } else {
                    semicolonTerminated=true;
                }

                pos++;
                break;

//				if( (pos+1) < lineLength ) {
////					toks.add(new Token(""+c, lineNumber, pos));
////					sb=null;
//					pos++; // skip over it
//				} else {
//					break; // ignore it
//				}
			} else {
				parseException("Unexpected character \"" + c + "\".)", lineNumber, pos);
			}
		}

        completeToken();

		// if toks are empty, try another line
		// this seems a bit dangerous, but eventually we should get a null line
		// which serves as a termination condition for the recursion
		while(toks != null && toks.isEmpty()) {
            toks = getLineTokens();
        }

		return toks;
	}

	private void completeToken() throws SDLParseException {
		if(sb != null) {
			toks.add(new Token(sb.toString(), lineNumber, tokenStart));
			sb = null;
		}
	}

	private void addEscapedCharInString(char c) throws SDLParseException {

		switch(c) {
			case '\\':
			case '"':
				sb.append(c);
				break;
			case 'n':
				sb.append('\n');
				break;
			case 'r':
				sb.append('\r');
				break;
			case 't':
				sb.append('\t');
				break;
			default:
				parseException("Ellegal escape character in string literal: \"" + c + "\".", lineNumber, pos);
		}
	}

	private void handleDoubleQuoteString() throws SDLParseException, IOException {
		boolean escaped=false;
		startEscapedQuoteLine=false;

		sb = new StringBuilder("\"");
		pos++;

		for(;pos<lineLength; pos++) {
			char c=line.charAt(pos);

			if(" \t".indexOf(c)!=-1 && startEscapedQuoteLine)
				continue;
			else
				startEscapedQuoteLine=false;

			if(escaped) {
				addEscapedCharInString(c);
				escaped=false;
			} else if(c=='\\') {
				// check for String broken across lines
				if(pos==lineLength-1 || (pos+1<lineLength &&
						" \t".indexOf(line.charAt(pos+1))!=-1)) {
					handleEscapedDoubleQuotedString();
				} else {
					escaped=true;
				}
			} else {
				sb.append(c);
				if(c=='"') {
					toks.add(new Token(sb.toString(), lineNumber, tokenStart));
					sb=null;
					return;
				}
			}
		}

		if(sb != null) {
			final String tokString = sb.toString();
			if(tokString.length()>0 && tokString.charAt(0)=='"' && tokString.charAt(tokString.length()-1)!='"') {
				parseException("String literal \"" + tokString + "\" not terminated by end quote.", lineNumber, line.length());
			} else if(tokString.length()==1 && tokString.charAt(0)=='"') {
				parseException("Orphan quote (unterminated string)", lineNumber, line.length());
			}
		}
	}

	private void handleEscapedDoubleQuotedString() throws SDLParseException, IOException {
		if(pos==lineLength-1) {
			line = readLine();
			if(line==null) {
				parseException("Escape at end of file.", lineNumber, pos);
			}

			lineLength = line.length();
			pos=-1;
			startEscapedQuoteLine=true;
		} else {
			// consume whitespace
			int j=pos+1;
			while(j<lineLength && " \t".indexOf(line.charAt(j))!=-1) j++;

			if(j==lineLength) {
				line = readLine();
				if(line==null) {
					parseException("Escape at end of file.", lineNumber, pos);
				}

				lineLength = line.length();
				pos=-1;
				startEscapedQuoteLine=true;

			} else {
				parseException("Malformed string literal - " +
						"escape followed by whitespace " +
						"followed by non-whitespace.", lineNumber,
						pos);
			}
		}
	}

	private void handleCharacterLiteral() throws SDLParseException {
		if(pos == lineLength-1) {
            parseException("Got ' at end of line", lineNumber, pos);
        }

		pos++;

		char c2 = line.charAt(pos);
		if(c2=='\\') {

			if(pos==lineLength-1) {
                parseException("Got '\\ at end of line", lineNumber, pos);
            }

			pos++;
			char c3 = line.charAt(pos);

			if(pos == lineLength-1) {
                parseException("Got '\\" + c3 + " at end of line", lineNumber, pos);
            }

			if(c3=='\\') {
				toks.add(new Token("'\\'", lineNumber, pos));
			} else if(c3=='\'') {
				toks.add(new Token("'''", lineNumber, pos));
			} else if(c3=='n') {
				toks.add(new Token("'\n'", lineNumber, pos));
			} else if(c3=='r') {
				toks.add(new Token("'\r'", lineNumber, pos));
			}  else if(c3=='t') {
				toks.add(new Token("'\t'", lineNumber, pos));
			} else {
				parseException("Illegal escape character " + line.charAt(pos), lineNumber, pos);
			}

			pos++;
			if(line.charAt(pos)!='\'') {
                expectingButGot("single quote (')", "\"" + line.charAt(pos) + "\"", lineNumber, pos);
            }
		} else {
			toks.add(new Token("'" +  c2 + "'", lineNumber, pos));
			if(pos==lineLength-1) {
                parseException("Got '" + c2 + " at end of " +  "line", lineNumber, pos);
            }
			pos++;
			if(line.charAt(pos)!='\'') {
                expectingButGot("quote (')", "\"" + line.charAt(pos) + "\"", lineNumber, pos);
            }
		}
	}

	private void handleSlashComment() throws SDLParseException, IOException {
		if(pos==lineLength-1) {
			parseException("Got slash (/) at end of line.", lineNumber, pos);
		}

		if(line.charAt(pos+1)=='*') {

			int endIndex = line.indexOf("*/", pos+1);
			if(endIndex!=-1) {
				// handle comment on same line
				pos=endIndex+1;
			} else {
				// handle multiline comments
				inner: while(true) {
					line = readRawLine();
					if(line==null) {
						parseException("/* comment not terminated.", lineNumber, -2);
					}

					endIndex = line.indexOf("*/");

					if(endIndex!=-1) {
						lineLength = line.length();
						pos=endIndex+1;
						break inner;
					}
				}
			}
		} else if(line.charAt(pos+1)=='/') {
			parseException("Got slash (/) in unexpected location.", lineNumber, pos);
		}
	}

	private void handleLiteralString() throws SDLParseException, IOException {
		int endIndex = line.indexOf("`", pos+1);

		if(endIndex != -1) {
			// handle end quote on same line
			toks.add(new Token(line.substring(pos, endIndex+1), lineNumber, pos));
			sb = null;

			pos = endIndex;
		} else {

			sb = new StringBuilder(line.substring(pos) + "\n");
			int start = pos;
			// handle multiline quotes
			inner: while(true) {
				line = readRawLine();
				if(line==null) {
					parseException("` quote not terminated.", lineNumber, -2);
				}

				endIndex = line.indexOf("`");
				if(endIndex!=-1) {
					sb.append(line.substring(0, endIndex+1));

					line=line.trim();
					lineLength = line.length();

					pos=endIndex;
					break inner;
				} else {
					sb.append(line + "\n");
				}
			}

			toks.add(new Token(sb.toString(), lineNumber, start));
			sb=null;
		}
	}

	private void handleBinaryLiteral() throws SDLParseException, IOException {
		int endIndex = line.indexOf("]", pos+1);

		if(endIndex != -1) {
			// handle end quote on same line
			toks.add(new Token(line.substring(pos, endIndex+1), lineNumber, pos));
			sb = null;

			pos=endIndex;
		} else {
			sb = new StringBuilder(line.substring(pos) + "\n");
			int start = pos;
			// handle multiline quotes
			inner: while(true) {
				line = readRawLine();
				if(line==null) {
					parseException("[base64] binary literal not terminated.", lineNumber, -2);
				}

				endIndex = line.indexOf("]");
				if(endIndex!=-1) {
					sb.append(line.substring(0, endIndex+1));

					line=line.trim();
					lineLength = line.length();

					pos=endIndex;
					break inner;
				} else {
					sb.append(line + "\n");
				}
			}

			toks.add(new Token(sb.toString(), lineNumber, start));
			sb=null;
		}
	}

	// handle a line continuation (not inside a string)
	private void handleLineContinuation() throws SDLParseException, IOException {
		if(line.substring(pos+1).trim().length()!=0) {
			parseException("Line continuation (\\) before end of line", lineNumber, pos);
		} else {
			line = readLine();
			if(line == null) {
				parseException("Line continuation at end of file.", lineNumber, pos);
			}

			lineLength = line.length();
			pos=-1;
		}
	}

	private void handleNumberDateOrTimeSpan() throws SDLParseException {
		tokenStart = pos;
		sb = new StringBuilder();
		char c;
		boolean allowSpace = line.substring(pos).matches(DATETIME_REGEX + ".*");

		for(;pos<lineLength; ++pos) {
			c=line.charAt(pos);

			if("0123456789.-+:abcdefghijklmnopqrstuvwxyz".indexOf(Character.toLowerCase(c)) != -1) {
				sb.append(c);
			} else if(c=='/' && !((pos+1)<lineLength && line.charAt(pos+1)=='*')) {
				sb.append(c);
			} else if(c == ' ' && allowSpace) {
			    sb.append(c);
			    allowSpace = false; // there's only 1 space in a datetime
            } else {
				pos--;
				break;
			}
		}

		toks.add(new Token(sb.toString(), lineNumber, tokenStart));
		sb=null;
	}

	private void handleIdentifier() throws SDLParseException {
		tokenStart=pos;
		sb=new StringBuilder();
		char c;

		for(;pos<lineLength; ++pos) {
			c=line.charAt(pos);

			if( String.valueOf(c).matches("(\\w|\\n|-|_|\\.|\\$)") ) {
				sb.append(c);
			} else {
				pos--;
				break;
			}
		}

		toks.add(new Token(sb.toString(), lineNumber, tokenStart));
		sb=null;
	}

	/**
	 * Close the reader and throw a SDLParseException
	 */
	private void parseException(final String description, final int line, final int position) throws SDLParseException {
		try {
			reader.close();
		} catch(final IOException ioe) { /* no recourse */ }

		// We add one because editors typically start with line 1 and position 1
		// rather than 0...
		throw new SDLParseException(description, line+1, position+1);
	}

	/**
	 * Close the reader and throw a SDLParseException using the format
	 * Was expecting X but got Y.
	 */
	private void expectingButGot(final String expecting,
								 final Object got,
								 final int line,
								 final int position) throws SDLParseException {
		parseException(
				String.format("Was expecting %s but got %s", expecting, String.valueOf(got)),
				line, position);
	}

	/**
	 * Skips comment lines and blank lines.
	 *
	 * @return the next line or null at the end of the file.
	 */
	@Nullable
	private String readLine() throws IOException {
		String line = reader.readLine();
		pos=0;

		if(line==null) {
            return null;
        }
		lineNumber++;

		String tLine = line.trim();

		while(tLine.startsWith("#") || tLine.length()==0) {
			line = reader.readLine();
			if(line==null) {
                return null;
            }

			lineNumber++;
			tLine = line.trim();
		}

		return line;
	}

	/**
	 * Reads a "raw" line including lines with comments and blank lines, increments line count.
	 *
	 * @return the next line or null at the end of the file.
	 */
	@Nullable
	private String readRawLine() throws IOException {
		final String line = reader.readLine();
        if(line == null) {
            return null;
        }

		pos=0;
		lineNumber++;

		return line;
	}


	////////////////////////////////////////////////////////////////////////////
	// Parsers for types
	////////////////////////////////////////////////////////////////////////////
	static String parseString(final String literal) {
		if(literal.charAt(0) != '"' && literal.charAt(literal.length()-1) != '"') {
		    throw new IllegalArgumentException("Malformed string <" + literal + ">.  Strings must start and end with \"");
        }

		return literal.substring(1, literal.length()-1);
	}

    static String parseMultilineString(final String literal) {
        if(literal.charAt(0) != '`' && literal.charAt(literal.length()-1) != '`') {
            throw new IllegalArgumentException("Malformed string <" + literal + ">.  String Literals must start and end with `");
        }

        return new String(literal.substring(1, literal.length() - 1).getBytes());
        //text.replaceAll("\\", "\\\\");
    }

	static Character parseCharacter(String literal) {
		if(literal.charAt(0)!='\'' || literal.charAt(literal.length()-1)!='\'') {
            throw new IllegalArgumentException("Malformed character <" +
                literal + ">. Character literals must start and end with single quotes.");
        }

		return Character.valueOf(literal.charAt(1));
	}

	static Number parseNumber(String literal) {
		int textLength = literal.length();
		boolean hasDot=false;
		int tailStart=0;

		for(int i=0; i < textLength; i++) {
			char c=literal.charAt(i);
			if("-0123456789".indexOf(c) == -1) {
				if(c=='.') {
					if(hasDot) {
						throw new NumberFormatException("Encountered second decimal point.");
					} else if(i == textLength-1) {
						throw new NumberFormatException("Encountered decimal point at the end of the number.");
					} else {
						hasDot=true;
					}
				} else {
					tailStart=i;
					break;
				}
			} else {
				tailStart=i+1;
			}
		}

		final String number = literal.substring(0, tailStart);
		final String tail = literal.substring(tailStart);


		if(tail.length() == 0) {
			if(hasDot) {
				return new Double(number);
			} else {
				return new Integer(number);
			}
		}

		if(tail.equalsIgnoreCase("BD")) {
			return new BigDecimal(number);
		} else if(tail.equalsIgnoreCase("L")) {
			if(hasDot) {
				throw new NumberFormatException("Long literal with decimal point");
			}
			return new Long(number);
		} else if(tail.equalsIgnoreCase("F")) {
			return new Float(number);
		} else if(tail.equalsIgnoreCase("D")) {
			return new Double(number);
		}

		throw new NumberFormatException("Could not parse number <" + literal + ">");
	}

	static LocalDate parseDate(final String literal) {
	    return LocalDate.parse(literal, DateTimeFormatter.ofPattern("y/M/d"));
    }

    static LocalTime parseTime(final String literal) {
        final String[] split = literal.split(":|\\.|-");

        //final StringBuilder pattern = new StringBuilder("H:m");

        //LocalTime.of()

        switch (split.length) {
            case 2: // H:m
                return LocalTime.parse(literal, DateTimeFormatter.ofPattern("H:m"));
                //return LocalTime.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            case 3: // H:m:s
                return LocalTime.parse(literal, DateTimeFormatter.ofPattern("H:m:s"));
                //return LocalTime.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            case 4: // H:m:s.S
                return LocalTime.parse(literal, DateTimeFormatter.ISO_LOCAL_TIME); //DateTimeFormatter.ofPattern("H:m:s.S")
            case 5:
                // can't handle time with a TimeZone but no Date
                return null;
//                return LocalTime.of(Integer.parseInt(split[0]),
//                    Integer.parseInt(split[1]),
//                    Integer.parseInt(split[2]),
//                    Integer.parseInt(split[3]));

//            case 1:
//                return LocalTime.parse(literal, DateTimeFormatter.ofPattern("H:m:s"));
//            case 2:
//                if(split[1].contains(".")) {
//                    return LocalTime.parse(literal, DateTimeFormatter.ofPattern("H:m:s.SSS"));
//                } // else we are trying to parse "HH:mm:ss-Z" which is not possible
//                break;
//            case 3:
//                // you cannot have ZonedTime without a date
//                //return LocalTime.parse(literal, DateTimeFormatter.ofPattern("HH:mm:ss.SSS-Z"));
//                break;
        }
        return null; // should there be an exception here???
    }

    static LocalDateTime parseLocalDateTime(final String literal) {
	    int[] intVals = {0,0,0,0,0,0,0};
        final String[] strVals = literal.split("/| |:|\\.");
        for (int i = 0; i < strVals.length; i++) {
            intVals[i] = Integer.parseInt(strVals[i]);
        }
        return LocalDateTime.of(intVals[0], intVals[1], intVals[2], intVals[3], intVals[4], intVals[5], intVals[6]*1_000_000);
    }

	static ZonedDateTime parseZonedDateTime(final String literal) {
        final TimeZone timeZone = literal.contains("-") ? TimeZone.getTimeZone(literal.substring(literal.indexOf("-") +1)) : TimeZone.getDefault();

        int[] intVals = {0,0,0,0,0,0,0};

        final String[] strVals = literal.contains("-") ? literal
            .substring(0, literal.lastIndexOf("-"))
            .split("/| |:|\\.") :
            literal.split("/| |:|\\.");

        for (int i = 0; i < strVals.length; i++) {
            intVals[i] = Integer.parseInt(strVals[i]);
        }

        return ZonedDateTime.of(intVals[0], intVals[1], intVals[2], intVals[3], intVals[4], intVals[5], intVals[6]*1_000_000, timeZone.toZoneId());
    }

	static byte[] parseBinary(String literal) {
		final String stripped = literal.substring(1, literal.length()-1);
		final StringBuilder sb = new StringBuilder();
		final int btLength = stripped.length();
		for(int i=0; i<btLength; i++) {
			char c = stripped.charAt(i);
			if("\n\r\t ".indexOf(c)==-1) {
                sb.append(c);
            }
		}

		return Base64.getDecoder().decode(sb.toString());
	}


	static Duration parseTimeSpan(final String literal) {
	    if(!literal.matches(TIMESPAN_REGEX)) {
            throw new IllegalArgumentException("Malformed time span <" +
                literal + ">.  Time spans must use the format " +
                "(d:)hh:mm:ss(.xxx) Note: if the day component is " +
                "included it must be suffixed with lower case \"d\"");
        }

        final boolean negate = literal.contains("-");
        final List<String> groups = Arrays.asList(literal.replace("-", "").split("d:|\\.")); // we should get 1 - 3 groups

        int days=0; // optional
		int hours=0; // mandatory
		int minutes=0; // mandatory
		int seconds=0; // mandatory
		long milliseconds=0; // optional

        for(final String group : groups) {
            if(group.matches("\\d+:\\d+:\\d+")) {
                final String[] segments = group.split(":");
                hours = Integer.parseInt(segments[0]);
                minutes = Integer.parseInt(segments[1]);
                seconds = Integer.parseInt(segments[2]);
            } else {
                if(groups.indexOf(group) == 0) {
                    days = Integer.parseInt(group);
                } else {
                    milliseconds = Long.parseLong(group);
                }
            }
        }

        final Duration duration = Duration.ofDays(days)
            .plusHours(hours)
            .plusMinutes(minutes)
            .plusSeconds(seconds)
            .plusMillis(milliseconds);

        return negate ? duration.negated() : duration;
    }

}
