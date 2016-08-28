package com;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;
import java.util.logging.*;
import mlogger.Log;

/**

 @author Miłosz Ziernik
 */
public final class StringUtils {

    private StringUtils() {
    }

    private static final String[][] htmlEscape = {{"&lt;", "<"}, {"&gt;", ">"},
    {"&amp;", "&"}, {"&quot;", "\""},
    {"&agrave;", "à"}, {"&Agrave;", "À"},
    {"&acirc;", "â"}, {"&auml;", "ä"},
    {"&Auml;", "Ä"}, {"&Acirc;", "Â"},
    {"&aring;", "å"}, {"&Aring;", "Å"},
    {"&aelig;", "æ"}, {"&AElig;", "Æ"},
    {"&ccedil;", "ç"}, {"&Ccedil;", "Ç"},
    {"&eacute;", "é"}, {"&Eacute;", "É"},
    {"&egrave;", "è"}, {"&Egrave;", "È"},
    {"&ecirc;", "ê"}, {"&Ecirc;", "Ê"},
    {"&euml;", "ë"}, {"&Euml;", "Ë"},
    {"&iuml;", "ï"}, {"&Iuml;", "Ï"},
    {"&ocirc;", "ô"}, {"&Ocirc;", "Ô"},
    {"&ouml;", "ö"}, {"&Ouml;", "Ö"},
    {"&oslash;", "ø"}, {"&Oslash;", "Ø"},
    {"&szlig;", "ß"}, {"&ugrave;", "ù"},
    {"&Ugrave;", "Ù"}, {"&ucirc;", "û"},
    {"&Ucirc;", "Û"}, {"&uuml;", "ü"},
    {"&Uuml;", "Ü"}, {"&nbsp;", " "},
    {"&copy;", "\u00a9"},
    {"&reg;", "\u00ae"},
    {"&euro;", "\u20a0"}
    };

    /**
     Konwertuje tekst na standard zapisu metody, np: "To
     jest_przykładowa-procedura" -> toHestPrzykladowaProcedura
     */
    public static String formatMethodName(String name) {
        return formatMethodName(name, false);
    }

    public static String formatMethodName(String name, boolean upper) {
        if (name == null)
            return "";

        name = convertPolishChars(name);

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (c == '_' || c == '-' || c == ' ' || c == '.' || c == ',' || c == ';') {
                upper = true;
                continue;
            }

            if (upper) {
                c = Character.toUpperCase(c);
                upper = false;
            }

            if (c != '_'
                    && !(c >= 'A' && c <= 'Z')
                    && !(c >= 'a' && c <= 'z')
                    && !(i > 0 && c >= '0' && c <= '9'))
                c = '_';

            out.append(c);
        }

        return out.toString();
    }

    public static String convertPolishChars(String text) {

        final String src = "ąćęśźńżółĄĆĘŚŹŃŻÓŁ";
        final String dest = "acesznzolACESZNZOL";
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int v = src.indexOf(c);
            res.append(v >= 0 ? dest.charAt(v) : c);

        }
        return res.toString();
    }

    public static String encodeURIComponent(String s) {
        if (s == null)
            return "";
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String decodeURIComponent(String s) {
        if (s == null)
            return "";
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public static String unescapeHTML(String s) {
        return unescapeHTML(s, 0);
    }

    public static String unescapeHTML(String s, int start) {
        int i, j, k;

        i = s.indexOf("&", start);
        start = i + 1;
        if (i > -1) {
            j = s.indexOf(";", i);
            if (j > i) {
                String temp = s.substring(i, j + 1);
                k = 0;
                while (k < htmlEscape.length)
                    if (htmlEscape[k][0].equals(temp))
                        break;
                    else
                        k++;
                if (k < htmlEscape.length) {
                    s = s.substring(0, i)
                            + htmlEscape[k][1] + s.substring(j + 1);
                    return unescapeHTML(s, i); // recursive call
                }
            }
        }
        return s;
    }

    public static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    public static String generateText(int minLength, int maxLength) {

        final String charsA = "aeiou";
        final String charsB = "bcdfghjklmnpqrstwvxyz";
        final String plchars = "ąęćśńółźż";
        final String sspec = ".,:;!?-";

        Random rand = new Random();
        int len = minLength + rand.nextInt(maxLength - minLength);
        StringBuilder sb = new StringBuilder();

        int word = rand.nextInt(15);
        int ispec = 2 + rand.nextInt(8);

        char c = ' ';
        int space = 0;
        while (sb.length() < len) {
            int r = rand.nextInt();

            String src = r % 15 == 0 ? plchars
                    : r % 3 == 0 ? charsB
                            : r % 2 == 0 ? charsA
                                    : null;

            if (src == null)
                continue;

            char x = src.charAt(rand.nextInt(src.length()));

            if (c == x)
                continue;

            c = x;

            if (sb.length() == 0)
                c = Character.toUpperCase(c);

            if (space > word) {
                char sc = sspec.charAt(rand.nextInt(sspec.length()));

                if (sc == '-')
                    sb.append(' ');

                --ispec;
                if (ispec <= 0) {
                    ispec = rand.nextInt(6);
                    sb.append(sc);

                    boolean upper = sc == '.' || sc == '!' || sc == '?';

                    if (upper)
                        c = Character.toUpperCase(c);

                    if (upper && rand.nextBoolean()) {
                        space = 0;
                        sb.append('\n').append(c);
                        continue;
                    }
                }

                if (rand.nextInt(6) % 6 == 0)
                    c = Character.toUpperCase(c);

                sb.append(' ');
                space = 0;
                word = 2 + rand.nextInt(15);
            }

            sb.append(c);
            space++;
        }

        sb.append('.');

        return sb.toString();

    }

    public static String escapeJS(Object object) {
        return escapeJS(object, true);
    }

    public static String escapeJS(Object object, boolean escapeUnicode) {
        if (object == null)
            return null;

        StringBuilder sb = new StringBuilder();
        try {
            if (object instanceof Boolean || object instanceof Number)
                sb.append(object.toString());
            else {
                sb.append("\"");
                escape(object.toString(), sb, escapeUnicode);
                sb.append("\"");
            }
        } catch (IOException ex) {
            Log.error(ex);
        }
        return sb.toString();
    }

    private static void escape(String string, Appendable w, boolean escapeUnicode) throws IOException {
        if (string == null || string.length() == 0)
            return;
        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    w.append('\\');
                    w.append(c);
                    break;
                case '/':
                    if (b == '<')
                        w.append('\\');
                    w.append(c);
                    break;
                case '\b':
                    w.append("\\b");
                    break;
                case '\t':
                    w.append("\\t");
                    break;
                case '\n':
                    w.append("\\n");
                    break;
                case '\f':
                    w.append("\\f");
                    break;
                case '\r':
                    w.append("\\r");
                    break;
                default:
                    if (c < 32 || (escapeUnicode && c > 127)) {
                        w.append("\\u");
                        hhhh = Integer.toHexString(c);
                        w.append("0000", 0, 4 - hhhh.length());
                        w.append(hhhh);
                    }
                    else
                        w.append(c);
            }
        }
    }

    public static String escapeJava(Object object) {
        return escapeJava(object, false);
    }

    /**
     Escapuje obiekt do posatci javowej, jeśli jest to string to dodaje cudzysłów
     @param object
     @param escapeUnicode
     @return 
     */
    public static String escapeJava(Object object, boolean escapeUnicode) {
        StringWriter sw = new StringWriter();
        try {

            if (object == null)
                sw.append("null");
            else
                if (object instanceof Boolean || object instanceof Number)
                    sw.append(object.toString());
                else {
                    sw.append("\"");
                    escapeJava(Utils.toString(object), sw, escapeUnicode);
                    sw.append("\"");
                }
        } catch (Exception e) {
        }
        return sw.toString();
    }

    public static void escapeJava(String string, Appendable w, boolean escapeUnicode) throws IOException {
        if (string == null || string.length() == 0)
            return;
        char c = 0;
        for (int i = 0; i < string.length(); i += 1) {
            char b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    w.append('\\')
                            .append(c);
                    break;
                case '/':
                    if (b == '<')
                        w.append('\\');
                    w.append(c);
                    break;
                case '\b':
                    w.append("\\b");
                    break;
                case '\t':
                    w.append("\\t");
                    break;
                case '\n':
                    w.append("\\n");
                    break;
                case '\f':
                    w.append("\\f");
                    break;
                case '\r':
                    w.append("\\r");
                    break;
                default:
                    if (c < 32 || (escapeUnicode && c > 127)) {
                        w.append("\\u");
                        String hhhh = Integer.toHexString(c);
                        w.append("0000", 0, 4 - hhhh.length())
                                .append(hhhh);
                    }
                    else
                        w.append(c);
            }
        }
    }

    public static String escapeXML(String s) {
        StringWriter writer = new StringWriter();
        try {
            escapeXML(writer, s);
        } catch (IOException ex) {
            Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();

    }

    public static void escapeXML(Appendable writer, String s) throws IOException {
        if (writer == null || s == null)
            return;

        int len = s.length();

        //  znaki zastrzeżone w XMLu 	[#x1-#x8] | [#xB-#xC] | [#xE-#x1F] | [#x7F-#x84] | [#x86-#x9F]        
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if ((c >= 0x0 && c <= 0x8)
                    || c == 0xB
                    || c == 0xC
                    || c == 0xE
                    || (c >= 0xF && c <= 0x1F)
                    || (c >= 0x7F && c <= 0x84)
                    || (c >= 0x86 && c <= 0x97))
                c = '.'; // znak zastępczy dla zastrzeżonych

            switch (c) {
                case '<':
                    writer.append("&lt;");
                    break;
                case '>':
                    writer.append("&gt;");
                    break;
                case '&':
                    writer.append("&amp;");
                    break;
                case '"':
                    writer.append("&quot;");
                    break;
                case '\'':
                    writer.append("&apos;");
                    break;
                default:
                    writer.append(c);
                    break;
            }
        }
    }

    public String unescapeJava(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     Usuń fragment ze stringa
     @param base
     @param substring
     @return 
     */
    public static String remove(String base, String substring) {
        if (base == null
                || base.isEmpty()
                || substring == null
                || substring.isEmpty()
                || !base.contains(substring))
            return base;

        return base.substring(0, base.indexOf(substring))
                + base.substring(base.indexOf(substring)
                        + substring.length(), base.length());

    }

    public final static String getUniqueName(String base,
            Iterable<String> elements) {
        return getUniqueName(base, elements, "%base%%index%", true);
    }

    /**
     Zwraca unikalną nazwę. Jeśli base występuje na liście, to dodawany jest 
     index i formatowany na podstawie maski
     @param base
     @param elements
     @param mask
     @param caseSensitive
     @return 
     */
    public final static String getUniqueName(String base,
            Iterable<String> elements, String mask, boolean caseSensitive) {

        if (base == null || elements == null)
            return base;

        if (mask == null || mask.isEmpty())
            mask = "%base%%index%";

        if (!mask.contains("%base%"))
            throw new RuntimeException("Parametr mask nie zawiera zmiennej \"%base%\"");
        if (!mask.contains("%index%"))
            throw new RuntimeException("Parametr mask nie zawiera zmiennej \"%index%\"");

        String newName = base;
        boolean unique = false;
        int counter = 1;
        while (!unique) {
            unique = true;
            for (String s : elements)
                if (s != null)
                    if ((caseSensitive && s.equals(newName))
                            || (!caseSensitive && s.equalsIgnoreCase(newName))) {
                        unique = false;
                        break;
                    }

            if (unique)
                break;

            newName = mask.replace("%index%", "" + ++counter).replace("%base%", newName);
        }

        return newName;
    }

    public static String join(String separator, String... values) {
        if (values == null)
            return "";

        StringBuilder sb = new StringBuilder();
        for (String s : values) {
            if (sb.length() > 0)
                sb.append(separator);
            sb.append(s);
        }
        return sb.toString();
    }

    public static String[] split(String val) {
        if (val == null || val.isEmpty())
            return new String[0];
        return val.replace("\r\n", "|").replace("\n", "|").split("\\|");
    }
}
