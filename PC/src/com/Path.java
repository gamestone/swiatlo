package com;

import com.utils.Strings;
import java.io.*;
import java.util.*;

/**
 Created on : 2012-06-04, 09:41:23 Author : Miłosz Ziernik
 */
public class Path {

    public class URIPath {

        public String protocol;
        public String username;
        public String password;
        public String host;
        public final Strings path = new Strings().setSeparator("/");
        public String query;

        public String toString() {
            return (protocol != null && !protocol.isEmpty() ? protocol : "file")
                    + "://"
                    + (username != null && !username.isEmpty() ? uri.username
                    + (password != null && !password.isEmpty() ? ":" + password : "")
                    + "@" : "")
                    + (host != null && !host.isEmpty() ? host : "")
                    + (!path.isEmpty() ? "/" + path.toString("/") : "")
                    + (query != null && !query.isEmpty() ? "?" + query : "");
        }
    }

    public final Strings elements = new Strings().setSeparator("/");
    public String separator = "/";

    private boolean absolute;
    public boolean caseSensitive = true;
    public final URIPath uri = new URIPath();
    private final static boolean isWindows
            = false;// System.getProperty("os.name").toLowerCase().contains("windows");

    private List<String> buildPath(Object... sPath) {
        List<String> list = new LinkedList<>();
        if (sPath == null)
            return list;

        boolean first = true;

        for (Object ss : sPath) {
            if (ss == null)
                continue;

            String elm = ss.toString();

            if (elm.contains("?")) {
                uri.query = elm.substring(elm.indexOf("?") + 1, elm.length());
                elm = elm.substring(0, elm.indexOf("?"));
            }

            absolute |= !isWindows && elements.isEmpty() && first && elm.startsWith("/");

            if (first && elm.contains("://")) {
                uri.protocol = elm.substring(0, elm.indexOf("://"));
                elm = elm.substring(uri.protocol.length() + 3, elm.length());
            }

            String[] lst = elm.replace("\\", "/").split("/");
            for (String s : lst) {
                if (s.trim().isEmpty())
                    continue;

                if (first && uri.protocol != null && !uri.protocol.isEmpty() && s.contains("@")) {
                    String a = s.substring(0, s.indexOf("@"));
                    s = s.substring(a.length() + 1, s.length());

                    String[] elms = a.split(":");
                    uri.username = elms[0];
                    if (elms.length > 1)
                        uri.password = elms[1];
                }

                list.add(s);
                first = false;
            }
            first = false;
        }

        return list;
    }

    public Path add(Object... path) {
        elements.addAll(buildPath(path));

        uri.host = null;
        uri.path.clear();

        uri.path.addAll(elements);
        uri.host = uri.path.first(true);
        return this;
    }

    public Path(Object... path) {
        add(path);
    }

    public String getFileName() {
        if (elements.isEmpty())
            return "";
        return elements.get(elements.size() - 1);
    }

    public File getFile(Object... path) {
        return new File(getPath(path));
    }

    public Path getParent() {
        String res = "";

        for (int i = 0; i < elements.size() - 1; i++)
            res += elements.get(i) + separator;
        return new Path(res);
    }

    public String getPath(Object... path) {
        Strings list = new Strings(elements, buildPath(path));

        return (absolute ? separator : "") + list.toString(separator);
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String getFileNameWithoutExt() {
        String s = getFileName();
        if (s.indexOf(".") < 0)
            return s;
        return s.substring(0, s.lastIndexOf("."));
    }

    public String getFileExt() {
        String s = getFileName();
        if (s.indexOf(".") < 0)
            return "";
        return s.substring(s.lastIndexOf(".") + 1);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public boolean isSame(String path) {
        if (path == null)
            return false;
        String s1 = new Path(path).toString();
        String s2 = toString();

        return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
    }

    public Path getRelativePath(Path parent) {
        Path result = new Path(this);
        if (parent == null)
            return result;

        int i = 0;
        while (i < parent.elements.size()) {
            String s = parent.elements.get(i);

            String d = result.elements.first(false);

            if (caseSensitive ? s.equals(d) : s.equalsIgnoreCase(d)) {
                result.elements.remove(0);
                result.absolute = false;
            } else
                break;

            ++i;
        }

        return result;
    }

    @Override
    public Path clone() {
        return new Path(this);
    }

    public boolean mask(String mask) {
        mask = mask.replaceAll("\\.", "\\\\.");
        mask = mask.replaceAll("\\*", ".*");
        mask = mask.replaceAll("\\?", ".");
        return toString().matches("(?iu)" + mask);
    }
}
