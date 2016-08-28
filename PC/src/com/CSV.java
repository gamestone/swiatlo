package com;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Miłosz Ziernik
 * 2014/02/04 
 */
public final class CSV {

    public static class CSVWriter implements Closeable {

        public static final int INITIAL_STRING_SIZE = 128;

        private Writer rawWriter;

        private PrintWriter pw;

        private char separator;

        private char quotechar;

        private char escapechar;

        private String lineEnd;

        /** The character used for escaping quotes. */
        public static final char DEFAULT_ESCAPE_CHARACTER = '"';

        /** The default separator to use if none is supplied to the constructor. */
        public static final char DEFAULT_SEPARATOR = ',';

        /**
         * The default quote character to use if none is supplied to the
         * constructor.
         */
        public static final char DEFAULT_QUOTE_CHARACTER = '"';

        /** The quote constant to use when you wish to suppress all quoting. */
        public static final char NO_QUOTE_CHARACTER = '\u0000';

        /** The escape constant to use when you wish to suppress all escaping. */
        public static final char NO_ESCAPE_CHARACTER = '\u0000';

        /** Default line terminator uses platform encoding. */
        public static final String DEFAULT_LINE_END = "\n";

        private ResultSetHelper resultService = new ResultSetHelperService();

        /**
         * Constructs CSVWriter using a comma for the separator.
         *
         * @param writer
         *            the writer to an underlying CSV source.
         */
        public CSVWriter(Writer writer) {
            this(writer, DEFAULT_SEPARATOR);
        }

        /**
         * Constructs CSVWriter with supplied separator.
         *
         * @param writer
         *            the writer to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries.
         */
        public CSVWriter(Writer writer, char separator) {
            this(writer, separator, DEFAULT_QUOTE_CHARACTER);
        }

        /**
         * Constructs CSVWriter with supplied separator and quote char.
         *
         * @param writer
         *            the writer to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         */
        public CSVWriter(Writer writer, char separator, char quotechar) {
            this(writer, separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
        }

        /**
         * Constructs CSVWriter with supplied separator and quote char.
         *
         * @param writer
         *            the writer to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param escapechar
         *            the character to use for escaping quotechars or escapechars
         */
        public CSVWriter(Writer writer, char separator, char quotechar, char escapechar) {
            this(writer, separator, quotechar, escapechar, DEFAULT_LINE_END);
        }

        /**
         * Constructs CSVWriter with supplied separator and quote char.
         *
         * @param writer
         *            the writer to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param lineEnd
         * 			  the line feed terminator to use
         */
        public CSVWriter(Writer writer, char separator, char quotechar, String lineEnd) {
            this(writer, separator, quotechar, DEFAULT_ESCAPE_CHARACTER, lineEnd);
        }

        /**
         * Constructs CSVWriter with supplied separator, quote char, escape char and line ending.
         *
         * @param writer
         *            the writer to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param escapechar
         *            the character to use for escaping quotechars or escapechars
         * @param lineEnd
         * 			  the line feed terminator to use
         */
        public CSVWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd) {
            this.rawWriter = writer;
            this.pw = new PrintWriter(writer);
            this.separator = separator;
            this.quotechar = quotechar;
            this.escapechar = escapechar;
            this.lineEnd = lineEnd;
        }

        /**
         * Writes the entire list to a CSV file. The list is assumed to be a
         * String[]
         *
         * @param allLines
         *            a List of String[], with each String[] representing a line of
         *            the file.
         */
        public void writeAll(List<String[]> allLines) {
            for (String[] line : allLines) {
                writeNext(line);
            }
        }

        protected void writeColumnNames(ResultSet rs)
                throws SQLException {

            writeNext(resultService.getColumnNames(rs));
        }

        /**
         * Writes the entire ResultSet to a CSV file.
         *
         * The caller is responsible for closing the ResultSet.
         *
         * @param rs the recordset to write
         * @param includeColumnNames true if you want column names in the output, false otherwise
         *
         * @throws java.io.IOException thrown by getColumnValue
         * @throws java.sql.SQLException thrown by getColumnValue
         */
        public void writeAll(java.sql.ResultSet rs, boolean includeColumnNames) throws SQLException, IOException {

            if (includeColumnNames) {
                writeColumnNames(rs);
            }

            while (rs.next()) {
                writeNext(resultService.getColumnValues(rs));
            }
        }

        /**
         * Writes the next line to the file.
         *
         * @param nextLine
         *            a string array with each comma-separated element as a separate
         *            entry.
         */
        public void writeNext(String[] nextLine) {

            if (nextLine == null)
                return;

            StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
            for (int i = 0; i < nextLine.length; i++) {

                if (i != 0) {
                    sb.append(separator);
                }

                String nextElement = nextLine[i];
                if (nextElement == null)
                    continue;
                if (quotechar != NO_QUOTE_CHARACTER)
                    sb.append(quotechar);

                sb.append(stringContainsSpecialCharacters(nextElement) ? processLine(nextElement) : nextElement);

                if (quotechar != NO_QUOTE_CHARACTER)
                    sb.append(quotechar);
            }

            sb.append(lineEnd);
            pw.write(sb.toString());

        }

        private boolean stringContainsSpecialCharacters(String line) {
            return line.indexOf(quotechar) != -1 || line.indexOf(escapechar) != -1;
        }

        protected StringBuilder processLine(String nextElement) {
            StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
            for (int j = 0; j < nextElement.length(); j++) {
                char nextChar = nextElement.charAt(j);
                if (escapechar != NO_ESCAPE_CHARACTER && nextChar == quotechar) {
                    sb.append(escapechar).append(nextChar);
                } else
                    if (escapechar != NO_ESCAPE_CHARACTER && nextChar == escapechar) {
                        sb.append(escapechar).append(nextChar);
                    } else {
                        sb.append(nextChar);
                    }
            }

            return sb;
        }

        /**
         * Flush underlying stream to writer.
         * 
         * @throws IOException if bad things happen
         */
        public void flush() throws IOException {

            pw.flush();

        }

        /**
         * Close the underlying stream writer flushing any buffered content.
         *
         * @throws IOException if bad things happen
         *
         */
        public void close() throws IOException {
            flush();
            pw.close();
            rawWriter.close();
        }

        /**
         *  Checks to see if the there has been an error in the printstream. 
         */
        public boolean checkError() {
            return pw.checkError();
        }

        public void setResultService(ResultSetHelper resultService) {
            this.resultService = resultService;
        }

    }

    public static class CSVReader implements Closeable {

        private BufferedReader br;

        private boolean hasNext = true;

        private CSVParser parser;

        private int skipLines;

        private boolean linesSkiped;

        /**
         * The default line to start reading.
         */
        public static final int DEFAULT_SKIP_LINES = 0;

        /**
         * Constructs CSVReader using a comma for the separator.
         * 
         * @param reader
         *            the reader to an underlying CSV source.
         */
        public CSVReader(Reader reader) {
            this(reader, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
        }

        /**
         * Constructs CSVReader with supplied separator.
         * 
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries.
         */
        public CSVReader(Reader reader, char separator) {
            this(reader, separator, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         * 
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         */
        public CSVReader(Reader reader, char separator, char quotechar) {
            this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES);
        }

        /**
         * Constructs CSVReader with supplied separator, quote char and quote handling
         * behavior.
         *
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param strictQuotes
         *            sets if characters outside the quotes are ignored
         */
        public CSVReader(Reader reader, char separator, char quotechar, boolean strictQuotes) {
            this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, DEFAULT_SKIP_LINES, strictQuotes);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         *
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param escape
         *            the character to use for escaping a separator or quote
         */
        public CSVReader(Reader reader, char separator,
                char quotechar, char escape) {
            this(reader, separator, quotechar, escape, DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         * 
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param line
         *            the line number to skip for start reading 
         */
        public CSVReader(Reader reader, char separator, char quotechar, int line) {
            this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, line, CSVParser.DEFAULT_STRICT_QUOTES);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         *
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param escape
         *            the character to use for escaping a separator or quote
         * @param line
         *            the line number to skip for start reading
         */
        public CSVReader(Reader reader, char separator, char quotechar, char escape, int line) {
            this(reader, separator, quotechar, escape, line, CSVParser.DEFAULT_STRICT_QUOTES);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         * 
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param escape
         *            the character to use for escaping a separator or quote
         * @param line
         *            the line number to skip for start reading
         * @param strictQuotes
         *            sets if characters outside the quotes are ignored
         */
        public CSVReader(Reader reader, char separator, char quotechar, char escape, int line, boolean strictQuotes) {
            this(reader, separator, quotechar, escape, line, strictQuotes, CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         * 
         * @param reader
         *            the reader to an underlying CSV source.
         * @param separator
         *            the delimiter to use for separating entries
         * @param quotechar
         *            the character to use for quoted elements
         * @param escape
         *            the character to use for escaping a separator or quote
         * @param line
         *            the line number to skip for start reading
         * @param strictQuotes
         *            sets if characters outside the quotes are ignored
         * @param ignoreLeadingWhiteSpace
         *            it true, parser should ignore white space before a quote in a field
         */
        public CSVReader(Reader reader, char separator, char quotechar, char escape, int line, boolean strictQuotes, boolean ignoreLeadingWhiteSpace) {
            this.br = new BufferedReader(reader);
            this.parser = new CSVParser(separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace);
            this.skipLines = line;
        }

        /**
         * Reads the entire file into a List with each element being a String[] of
         * tokens.
         * 
         * @return a List of String[], with each String[] representing a line of the
         *         file.
         * 
         * @throws IOException
         *             if bad things happen during the read
         */
        public List<String[]> readAll() throws IOException {

            List<String[]> allElements = new ArrayList<String[]>();
            while (hasNext) {
                String[] nextLineAsTokens = readNext();
                if (nextLineAsTokens != null)
                    allElements.add(nextLineAsTokens);
            }
            return allElements;

        }

        /**
         * Reads the next line from the buffer and converts to a string array.
         * 
         * @return a string array with each comma-separated element as a separate
         *         entry.
         * 
         * @throws IOException
         *             if bad things happen during the read
         */
        public String[] readNext() throws IOException {

            String[] result = null;
            do {
                String nextLine = getNextLine();
                if (!hasNext) {
                    return result; // should throw if still pending?
                }
                String[] r = parser.parseLineMulti(nextLine);
                if (r.length > 0) {
                    if (result == null) {
                        result = r;
                    } else {
                        String[] t = new String[result.length + r.length];
                        System.arraycopy(result, 0, t, 0, result.length);
                        System.arraycopy(r, 0, t, result.length, r.length);
                        result = t;
                    }
                }
            } while (parser.isPending());
            return result;
        }

        /**
         * Reads the next line from the file.
         * 
         * @return the next line from the file without trailing newline
         * @throws IOException
         *             if bad things happen during the read
         */
        private String getNextLine() throws IOException {
            if (!this.linesSkiped) {
                for (int i = 0; i < skipLines; i++) {
                    br.readLine();
                }
                this.linesSkiped = true;
            }
            String nextLine = br.readLine();
            if (nextLine == null) {
                hasNext = false;
            }
            return hasNext ? nextLine : null;
        }

        /**
         * Closes the underlying reader.
         * 
         * @throws IOException if the close fails
         */
        public void close() throws IOException {
            br.close();
        }

    }

    public static class CSVParser {

        private final char separator;

        private final char quotechar;

        private final char escape;

        private final boolean strictQuotes;

        private String pending;
        private boolean inField = false;

        private final boolean ignoreLeadingWhiteSpace;

        /**
         * The default separator to use if none is supplied to the constructor.
         */
        public static final char DEFAULT_SEPARATOR = ',';

        public static final int INITIAL_READ_SIZE = 128;

        /**
         * The default quote character to use if none is supplied to the
         * constructor.
         */
        public static final char DEFAULT_QUOTE_CHARACTER = '"';

        /**
         * The default escape character to use if none is supplied to the
         * constructor.
         */
        public static final char DEFAULT_ESCAPE_CHARACTER = '\\';

        /**
         * The default strict quote behavior to use if none is supplied to the
         * constructor
         */
        public static final boolean DEFAULT_STRICT_QUOTES = false;

        /**
         * The default leading whitespace behavior to use if none is supplied to the
         * constructor
         */
        public static final boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;

        /**
         * This is the "null" character - if a value is set to this then it is ignored.
         * I.E. if the quote character is set to null then there is no quote character.
         */
        public static final char NULL_CHARACTER = '\0';

        /**
         * Constructs CSVParser using a comma for the separator.
         */
        public CSVParser() {
            this(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
        }

        /**
         * Constructs CSVParser with supplied separator.
         *
         * @param separator the delimiter to use for separating entries.
         */
        public CSVParser(char separator) {
            this(separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
        }

        /**
         * Constructs CSVParser with supplied separator and quote char.
         *
         * @param separator the delimiter to use for separating entries
         * @param quotechar the character to use for quoted elements
         */
        public CSVParser(char separator, char quotechar) {
            this(separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         *
         * @param separator the delimiter to use for separating entries
         * @param quotechar the character to use for quoted elements
         * @param escape    the character to use for escaping a separator or quote
         */
        public CSVParser(char separator, char quotechar, char escape) {
            this(separator, quotechar, escape, DEFAULT_STRICT_QUOTES);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         * Allows setting the "strict quotes" flag
         *
         * @param separator    the delimiter to use for separating entries
         * @param quotechar    the character to use for quoted elements
         * @param escape       the character to use for escaping a separator or quote
         * @param strictQuotes if true, characters outside the quotes are ignored
         */
        public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes) {
            this(separator, quotechar, escape, strictQuotes, DEFAULT_IGNORE_LEADING_WHITESPACE);
        }

        /**
         * Constructs CSVReader with supplied separator and quote char.
         * Allows setting the "strict quotes" and "ignore leading whitespace" flags
         *
         * @param separator               the delimiter to use for separating entries
         * @param quotechar               the character to use for quoted elements
         * @param escape                  the character to use for escaping a separator or quote
         * @param strictQuotes            if true, characters outside the quotes are ignored
         * @param ignoreLeadingWhiteSpace if true, white space in front of a quote in a field is ignored
         */
        public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace) {
            if (anyCharactersAreTheSame(separator, quotechar, escape)) {
                throw new UnsupportedOperationException("The separator, quote, and escape characters must be different!");
            }
            if (separator == NULL_CHARACTER) {
                throw new UnsupportedOperationException("The separator character must be defined!");
            }
            this.separator = separator;
            this.quotechar = quotechar;
            this.escape = escape;
            this.strictQuotes = strictQuotes;
            this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
        }

        private boolean anyCharactersAreTheSame(char separator, char quotechar, char escape) {
            return isSameCharacter(separator, quotechar) || isSameCharacter(separator, escape) || isSameCharacter(quotechar, escape);
        }

        private boolean isSameCharacter(char c1, char c2) {
            return c1 != NULL_CHARACTER && c1 == c2;
        }

        /**
         * @return true if something was left over from last call(s)
         */
        public boolean isPending() {
            return pending != null;
        }

        public String[] parseLineMulti(String nextLine) throws IOException {
            return parseLine(nextLine, true);
        }

        public String[] parseLine(String nextLine) throws IOException {
            return parseLine(nextLine, false);
        }

        /**
         * Parses an incoming String and returns an array of elements.
         *
         * @param nextLine the string to parse
         * @param multi
         * @return the comma-tokenized list of elements, or null if nextLine is null
         * @throws IOException if bad things happen during the read
         */
        private String[] parseLine(String nextLine, boolean multi) throws IOException {

            if (!multi && pending != null) {
                pending = null;
            }

            if (nextLine == null) {
                if (pending != null) {
                    String s = pending;
                    pending = null;
                    return new String[]{s};
                } else {
                    return null;
                }
            }

            List<String> tokensOnThisLine = new ArrayList<String>();
            StringBuilder sb = new StringBuilder(INITIAL_READ_SIZE);
            boolean inQuotes = false;
            if (pending != null) {
                sb.append(pending);
                pending = null;
                inQuotes = true;
            }
            for (int i = 0; i < nextLine.length(); i++) {

                char c = nextLine.charAt(i);
                if (c == this.escape) {
                    if (isNextCharacterEscapable(nextLine, inQuotes || inField, i)) {
                        sb.append(nextLine.charAt(i + 1));
                        i++;
                    }
                } else
                    if (c == quotechar) {
                        if (isNextCharacterEscapedQuote(nextLine, inQuotes || inField, i)) {
                            sb.append(nextLine.charAt(i + 1));
                            i++;
                        } else {
                    //inQuotes = !inQuotes;

                            // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                            if (!strictQuotes) {
                                if (i > 2 //not on the beginning of the line
                                        && nextLine.charAt(i - 1) != this.separator //not at the beginning of an escape sequence
                                        && nextLine.length() > (i + 1)
                                        && nextLine.charAt(i + 1) != this.separator //not at the	end of an escape sequence
                                        ) {

                                    if (ignoreLeadingWhiteSpace && sb.length() > 0 && isAllWhiteSpace(sb)) {
                                        sb.setLength(0);  //discard white space leading up to quote
                                    } else {
                                        sb.append(c);
                                        //continue;
                                    }

                                }
                            }

                            inQuotes = !inQuotes;
                        }
                        inField = !inField;
                    } else
                        if (c == separator && !inQuotes) {
                            tokensOnThisLine.add(sb.toString());
                            sb.setLength(0); // start work on next token
                            inField = false;
                        } else {
                            if (!strictQuotes || inQuotes) {
                                sb.append(c);
                                inField = true;
                            }
                        }
            }
            // line is done - check status
            if (inQuotes) {
                if (multi) {
                    // continuing a quoted section, re-append newline
                    sb.append("\n");
                    pending = sb.toString();
                    sb = null; // this partial content is not to be added to field list yet
                } else {
                    throw new IOException("Un-terminated quoted field at end of CSV line");
                }
            }
            if (sb != null) {
                tokensOnThisLine.add(sb.toString());
            }
            return tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);

        }

        /**
         * precondition: the current character is a quote or an escape
         *
         * @param nextLine the current line
         * @param inQuotes true if the current context is quoted
         * @param i        current index in line
         * @return true if the following character is a quote
         */
        private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
            return inQuotes // we are in quotes, therefore there can be escaped quotes in here.
                    && nextLine.length() > (i + 1) // there is indeed another character to check.
                    && nextLine.charAt(i + 1) == quotechar;
        }

        /**
         * precondition: the current character is an escape
         *
         * @param nextLine the current line
         * @param inQuotes true if the current context is quoted
         * @param i        current index in line
         * @return true if the following character is a quote
         */
        protected boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i) {
            return inQuotes // we are in quotes, therefore there can be escaped quotes in here.
                    && nextLine.length() > (i + 1) // there is indeed another character to check.
                    && (nextLine.charAt(i + 1) == quotechar || nextLine.charAt(i + 1) == this.escape);
        }

        /**
         * precondition: sb.length() > 0
         *
         * @param sb A sequence of characters to examine
         * @return true if every character in the sequence is whitespace
         */
        protected boolean isAllWhiteSpace(CharSequence sb) {
            boolean result = true;
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);

                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            return result;
        }
    }

    public static interface ResultSetHelper {

        public String[] getColumnNames(ResultSet rs) throws SQLException;

        public String[] getColumnValues(ResultSet rs) throws SQLException, IOException;
    }

    public static class ResultSetHelperService implements ResultSetHelper {

        public static final int CLOBBUFFERSIZE = 2048;

    // note: we want to maintain compatibility with Java 5 VM's
        // These types don't exist in Java 5
        private static final int NVARCHAR = -9;
        private static final int NCHAR = -15;
        private static final int LONGNVARCHAR = -16;
        private static final int NCLOB = 2011;

        public String[] getColumnNames(ResultSet rs) throws SQLException {
            List<String> names = new ArrayList<String>();
            ResultSetMetaData metadata = rs.getMetaData();

            for (int i = 0; i < metadata.getColumnCount(); i++) {
                names.add(metadata.getColumnName(i + 1));
            }

            String[] nameArray = new String[names.size()];
            return names.toArray(nameArray);
        }

        public String[] getColumnValues(ResultSet rs) throws SQLException, IOException {

            List<String> values = new ArrayList<String>();
            ResultSetMetaData metadata = rs.getMetaData();

            for (int i = 0; i < metadata.getColumnCount(); i++) {
                values.add(getColumnValue(rs, metadata.getColumnType(i + 1), i + 1));
            }

            String[] valueArray = new String[values.size()];
            return values.toArray(valueArray);
        }

        private String handleObject(Object obj) {
            return obj == null ? "" : String.valueOf(obj);
        }

        private String handleBigDecimal(BigDecimal decimal) {
            return decimal == null ? "" : decimal.toString();
        }

        private String handleLong(ResultSet rs, int columnIndex) throws SQLException {
            long lv = rs.getLong(columnIndex);
            return rs.wasNull() ? "" : Long.toString(lv);
        }

        private String handleInteger(ResultSet rs, int columnIndex) throws SQLException {
            int i = rs.getInt(columnIndex);
            return rs.wasNull() ? "" : Integer.toString(i);
        }

        private String handleDate(ResultSet rs, int columnIndex) throws SQLException {
            java.sql.Date date = rs.getDate(columnIndex);
            String value = null;
            if (date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                value = dateFormat.format(date);
            }
            return value;
        }

        private String handleTime(Time time) {
            return time == null ? null : time.toString();
        }

        private String handleTimestamp(Timestamp timestamp) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            return timestamp == null ? null : timeFormat.format(timestamp);
        }

        private String getColumnValue(ResultSet rs, int colType, int colIndex)
                throws SQLException, IOException {

            String value = "";

            switch (colType) {
                case Types.BIT:
                case Types.JAVA_OBJECT:
                    value = handleObject(rs.getObject(colIndex));
                    break;
                case Types.BOOLEAN:
                    boolean b = rs.getBoolean(colIndex);
                    value = Boolean.valueOf(b).toString();
                    break;
                case NCLOB: // todo : use rs.getNClob
                case Types.CLOB:
                    Clob c = rs.getClob(colIndex);
                    if (c != null) {
                        value = read(c);
                    }
                    break;
                case Types.BIGINT:
                    value = handleLong(rs, colIndex);
                    break;
                case Types.DECIMAL:
                case Types.DOUBLE:
                case Types.FLOAT:
                case Types.REAL:
                case Types.NUMERIC:
                    value = handleBigDecimal(rs.getBigDecimal(colIndex));
                    break;
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                    value = handleInteger(rs, colIndex);
                    break;
                case Types.DATE:
                    value = handleDate(rs, colIndex);
                    break;
                case Types.TIME:
                    value = handleTime(rs.getTime(colIndex));
                    break;
                case Types.TIMESTAMP:
                    value = handleTimestamp(rs.getTimestamp(colIndex));
                    break;
                case NVARCHAR: // todo : use rs.getNString
                case NCHAR: // todo : use rs.getNString
                case LONGNVARCHAR: // todo : use rs.getNString
                case Types.LONGVARCHAR:
                case Types.VARCHAR:
                case Types.CHAR:
                    value = rs.getString(colIndex);
                    break;
                default:
                    value = "";
            }

            if (value == null) {
                value = "";
            }

            return value;

        }

        private static String read(Clob c) throws SQLException, IOException {
            StringBuilder sb = new StringBuilder((int) c.length());
            Reader r = c.getCharacterStream();
            char[] cbuf = new char[CLOBBUFFERSIZE];
            int n;
            while ((n = r.read(cbuf, 0, cbuf.length)) != -1) {
                sb.append(cbuf, 0, n);
            }
            return sb.toString();
        }
    }

}
