package com;

import com.utils.Strings;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Miłosz Ziernik 2013/01/28
 */
public class Exceptions {

    /**
     * Klasa niskopoziomowych wyjątków serwera związana np z konfiguracją usługi
     */
    public static class CoreException extends RuntimeException {

        public CoreException(String message) {
            super(message);
        }

        public CoreException(Throwable cause) {
            super(cause);
        }
    }

    public abstract static interface IExceptionHandler {

        public abstract void onException(Exception ex);
    }

    public static String toString(Throwable ex) {
        return toString(ex, false);
    }

    /**
     * Zwraca komunikat błędu Jeśli treść jest null-em wtedy następuje
     * zagłębienie w strukturę
     * @param ex
     * @return 
     */
    public static String toString(Throwable ex, boolean inclClasses) {
        if (ex == null)
            return "";

        Throwable e = ex;
        String msg = "";
        String prev = null;

        Strings clsStack = new Strings();

        e = ex;
        while ((e = e.getCause()) != null) {
            clsStack.add(e.getClass().getName());
        }
        
        e = ex;
        Strings dispClsStack = new Strings();
        while ((e = e.getCause()) != null) {
            dispClsStack.add(e.getClass().getSimpleName());
        }
        
        e = ex;
        while (e != null) {
            if (e.getLocalizedMessage() != null || e instanceof NullPointerException) {

                String s = e instanceof NullPointerException ? "Null pointer" : e.getLocalizedMessage();

                if (s != null && !clsStack.isEmpty()
                        && s.startsWith(clsStack.toString(": ") + ": ")) {
                    String cs = clsStack.toString(": ");
                    s = s.substring(cs.length() + 1).trim();
                    clsStack.first(true);
                }

                if (s == null)
                    s = "NULL";

                if (!s.equals(prev)) {
                    if (!msg.isEmpty())
                        msg += ": ";
                    msg += s;
                }
                prev = s;
            }
            e = e.getCause();
        }
        return (inclClasses ? dispClsStack.toString(", ") + ": " : "") + msg;
    }

    public static String exceptionToStr(Throwable e) {
        return exceptionToStr(e, false);
    }

    public static String getClassName(Throwable ex) {
        if (ex == null)
            return "";

        List<String> names = new LinkedList<>();

        Throwable e = ex;
        while (e != null) {
            String name = e.getClass().getSimpleName();
            if (!names.contains(name))
                names.add(name);
            e = e.getCause();
        }
        return Utils.listToString(names);
    }

    public static String exceptionToStr(Throwable ex, boolean addStackTrace) {
        if (ex == null)
            return "null";

        StringWriter sw = new StringWriter();
        sw.append("[").append(getClassName(ex));

        sw.write("] ");
        sw.write(toString(ex));

        if (addStackTrace) {
            sw.write("\n\n");
            ex.printStackTrace(new PrintWriter(sw));
        }

        return sw.toString().trim();
    }

    private static List<StackTraceElement> getTrimmedStackTrace(StackTraceElement[] stackTrace) {
        List<StackTraceElement> lst = new LinkedList<>();

        final String[] exclude = {
            "javax.servlet.http.HttpServlet",
            "org.apache.catalina.core.ApplicationFilterChain",
            "org.apache.catalina.session.StandardSession",
            "org.apache.catalina.core.StandardContext"};

        for (StackTraceElement ste : stackTrace) {
            boolean cut = false;
            for (String s : exclude)
                if (s.equals(ste.getClassName())) {
                    cut = true;
                    break;
                }
            if (cut)
                break;
            lst.add(ste);
        }

        if (lst.isEmpty())
            lst.addAll(Arrays.asList(stackTrace));

        return lst;
    }

    private static Strings stackTraceToString(List<StackTraceElement> stackTrace) {
        Strings result = new Strings();
        result.setSeparator("\n");

        for (StackTraceElement ste : stackTrace) {
            StringBuilder sb = new StringBuilder();
            if (ste == null) {
                sb.append("[...]\n");
                continue;
            }
            sb.append(ste.getClassName()).append(".").append(ste.getMethodName());

            if (ste.getFileName() != null && ste.getLineNumber() > 0) {
                sb.append(" (").append(ste.getFileName()).append(":");
                sb.append(ste.getLineNumber()).append(")");
            }
            result.add(sb.toString());
        }
        return result;
    }

    public static List<StackTraceElement> getStackTrace(int level) {
        if (level < -2)
            level = -2;
        List<StackTraceElement> st
                = getTrimmedStackTrace(Thread.currentThread().getStackTrace());
        if (st.size() > 2)
            for (int i = 0; i < level + 2; i++)
                st.remove(0);
        return st;
    }

    public static List<StackTraceElement> getStackTrace(Throwable e) {
        return getTrimmedStackTrace(e.getStackTrace());
    }

    public static Strings getStackTraceStr(Throwable ex) {
        return stackTraceToString(getStackTrace(ex));
    }

    public static Strings getStackTraceStr() {
        return stackTraceToString(getStackTrace(1));
    }
}
