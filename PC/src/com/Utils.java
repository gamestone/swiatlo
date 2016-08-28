package com;

import mlogger.Log;
import com.sun.management.OperatingSystemMXBean;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import mlogger.*;

/**
 *
 * @author Miłosz Ziernik
 */
public final class Utils {

    public final static String fullDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static Collator collator = Collator.getInstance(new Locale("pl", "PL"));

    public static String formatDate(Date date) {
        return date == null ? null
               : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String formatDateMs(Date date) {
        return date == null ? null
               : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date);
    }

    public static String randomId() {
        return randomId(10);
    }

    public static String randomId(int len) {
        final String chars = "abcdefghijklmnopqrstuwvxyzABCDEFGHIJKLMNOPQRSTUWVXYZ1234567890";

        Random ran = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(ran.nextInt(chars.length() - ((i == 0) ? 10 : 0))));
        return sb.toString();
    }

    /**
     * Wyświetla ułamek jako wartość procentową.
     */
    public static String formatPercent(double d, int dec) {
        d *= 100;
        double pow = Math.pow(10, dec);
        return Double.toString(Math.round(d * pow) / pow) + " %";
    }

    public static String formatPercent(double d) {
        return formatFloat(100d * d) + " %";
    }

    public static String toString(Object obj) {
        return toString(obj, false);
    }

    public static String toString(Object obj, boolean checkToStringOverloaded) {
        if (obj == null)
            return null;

        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");

            for (int i = 0; i < Array.getLength(obj); i++) {

                if (sb.length() > 1)
                    sb.append(", ");
                sb.append(toString(Array.get(obj, i), checkToStringOverloaded));

            }
            sb.append("]");
            return sb.toString();
        }

        if (checkToStringOverloaded) {
            // sprawdza czy obiekt ma przeciążoną metodę toString
            String cls = obj.getClass().getName();
            String name = obj.toString();

            if (name.startsWith(cls)
                    && name.length() == cls.length() + 9
                    && name.charAt(cls.length()) == '@')
                Log.warning("Html Tag", "Obiekt " + cls + " nie ma przeciążonej metody toString");
        }

        return obj.toString();
    }

    public static List<String> asList(String... elements) {
        List<String> lst = new LinkedList<>();
        if (elements != null)
            for (String s : elements)
                lst.add(s);
        return lst;
    }

    public static StackTraceElement getCurrentStackTraceElement(int level) {
        StackTraceElement[] str = Thread.currentThread().getStackTrace();
        level += 2;
        if (level < 0 || level >= str.length)
            return null;
        StackTraceElement ste = str[level];
        return ste;
    }

    public static String getCurrentMethodName(int level) {
        StackTraceElement ste = getCurrentStackTraceElement(level + 1);
        return ste != null ? ste.getMethodName() + " [" + ste.getFileName()
                + ":" + ste.getLineNumber() + "]" : null;
    }

    /**
     * Czy obiekty sa identyczne. Porownywane sa kolekcje (Iterable) oraz
     * obiejty dziedziczące po Number
     */
    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;

        if (o1 != null && o1.equals(o2))
            return true;

        if (o2 != null && o2.equals(o1))
            return true;

        if (o1 instanceof Number && o2 instanceof Number)

            if (((Number) o1).longValue()
                    == ((Number) o2).longValue()
                    || ((Number) o1).doubleValue()
                    == ((Number) o2).doubleValue())
                return true;

        if (o1 instanceof Iterable && o2 instanceof Iterable) {

            Iterator i1 = ((Iterable) o1).iterator();
            Iterator i2 = ((Iterable) o2).iterator();

            if (i1.hasNext() != i2.hasNext())
                return false;

            while (i1.hasNext() && i2.hasNext())
                if (!equals(i1.next(), i2.next()))
                    return false;

            if (i1.hasNext() != i2.hasNext())
                return false;

            return true;
        }

        return false;
    }

    public static void invokeMethod(Method method, Object methodObject, Object... params) {
        try {
            method.setAccessible(true);
            method.invoke(methodObject, params);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public abstract static interface IExceptionHandler {

        public abstract void onException(Exception ex);
    }

    public interface IDelayRun {

        public void onExecute(Object object);
    }

    private static class DelayRun extends Thread {

        private Object object;
        private IDelayRun intf;
        private int delay;

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
                intf.onExecute(object);
            } catch (InterruptedException ex) {
            }
        }
    }

    public static void delayRun(int delay, IDelayRun intf, Object object) {
        if (intf == null)
            return;
        DelayRun run = new DelayRun();
        run.object = object;
        run.intf = intf;
        run.delay = delay;
        run.start();
    }

    public static InetSocketAddress parseSocketAddress(String s, int defaultPort) {
        if (s == null || s.trim().isEmpty())
            return null;
        final String chars = "01234567890abcdefghijklmnopqrstuwxyz-_.:@";
        try {
            s = s.replace(" ", "").trim();
            for (char c : s.toLowerCase().toCharArray())
                if (chars.indexOf(c) < 0)
                    return null;
            int port = defaultPort;
            if (s.contains(":")) {
                String p = s.substring(s.lastIndexOf(":") + 1);
                port = Integer.parseInt(p.trim());
                s = s.substring(0, s.lastIndexOf(":"));
            }
            return new InetSocketAddress(s.trim(), port);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formatowanie waluty
     */
    public static String formatCurrency(Float val, String separator) {
        if (val == null)
            return null;
        return formatCurrency((double) val, separator, false);
    }

    public static String formatCurrency(Double val) {
        return formatCurrency(val, ",", false);
    }

    public static String formatCurrency(Double val, String separator, boolean addSpaces) {
        if (val == null)
            return null;

        double d = Math.round(val * 100d) / 100d;
        String s = Double.toString(d);
        s = s.replace(",", ".");
        if (!s.contains("."))
            return s + ".00";
        String t = s.substring(s.indexOf(".") + 1);
        while (t.length() < 2)
            t += "0";
        s = s.substring(0, s.indexOf("."));

        if (addSpaces) {
            String ss = "";

            for (int i = 0; i < s.length(); i++) {
                ss = s.charAt(s.length() - i - 1) + ss;
                if (((i + 1) % 3) == 0)
                    ss = " " + ss;
            }
            s = ss;
        }
        return (s + "." + t).replace(".", separator);
    }

    public static boolean matchesMask(String fileName, String mask) {
        if (mask == null || fileName == null)
            return false;
        mask = mask.toLowerCase();
        fileName = fileName.toLowerCase();
        mask = mask.replaceAll("\\.", "\\\\.");
        mask = mask.replaceAll("\\*", ".*");
        mask = mask.replaceAll("\\?", ".");
        //    mask = mask.replaceAll("\\.", "[.]").replaceAll("*", ".*").replaceAll("?", ".");
        return fileName.matches(mask);
    }

    public static boolean arrayContains(final String[] arr, final String str) {
        if (arr == null || str == null)
            return false;
        for (String s : arr)
            if (s.equals(str))
                return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public static boolean contains(final Collection lst, final Object value) {
        if (lst == null || lst.isEmpty())
            return false;
        for (Iterator<Object> it = lst.iterator(); it.hasNext();) {
            Object val = it.next();
            if ((value == null && val == null) || (value != null && value.equals(val)))
                return true;
        }
        return false;
    }

    public static boolean listContains(final List<String> lst, final String str) {
        if (lst == null || str == null)
            return false;
        for (Iterator<String> it = lst.iterator(); it.hasNext();)
            if (str.equals(it.next()))
                return true;
        return false;
    }

    public final static class MemoryInfo {

        public class MemoryInfoItem {

            public final String name;
            public final long value;
            public final String unit;

            public MemoryInfoItem(String name, long value) {
                this.name = name;
                this.value = value;
                this.unit = Utils.formatFileSize(value);

            }
        }
        public final long freePhysicalMemorySize;
        public final long totalPhysicalMemorySize;
        public final long freeSwapSpaceSize;
        public final long totalSwapSpaceSize;
        public final long heapUsed;
        public final long heapCommitted;
        public final long heapMax;
        public final long heapInit;
        public final long noneHeapUsed;
        public final long noneHeapCommitted;
        public final long noneHeapMax;
        public final long noneHeapInit;
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        public MemoryInfo() {
            freePhysicalMemorySize = osBean.getFreePhysicalMemorySize();
            totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize();
            freeSwapSpaceSize = osBean.getFreeSwapSpaceSize();
            totalSwapSpaceSize = osBean.getTotalSwapSpaceSize();

            MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = mbean.getHeapMemoryUsage();
            MemoryUsage nheap = mbean.getNonHeapMemoryUsage();

            heapUsed = heap.getUsed();
            heapCommitted = heap.getCommitted();
            heapMax = heap.getMax();
            heapInit = heap.getInit();
            noneHeapUsed = nheap.getUsed();
            noneHeapCommitted = nheap.getCommitted();
            noneHeapMax = nheap.getMax();
            noneHeapInit = nheap.getInit();
        }

        public List<MemoryInfoItem> getAsList() {
            List<MemoryInfoItem> lst = new LinkedList<>();
            lst.add(new MemoryInfoItem("Free Physical Memory Size", freePhysicalMemorySize));
            lst.add(new MemoryInfoItem("Total Physical Memory Size", totalPhysicalMemorySize));
            lst.add(new MemoryInfoItem("Free Swap Space Size", freeSwapSpaceSize));
            lst.add(new MemoryInfoItem("Total Swap Space Size", totalSwapSpaceSize));
            lst.add(new MemoryInfoItem("Heap Used", heapUsed));
            lst.add(new MemoryInfoItem("Heap Committed", heapCommitted));
            lst.add(new MemoryInfoItem("Heap Max", heapMax));
            lst.add(new MemoryInfoItem("Heap Init", heapInit));
            lst.add(new MemoryInfoItem("None Heap Used", noneHeapUsed));
            lst.add(new MemoryInfoItem("None Heap Committed", noneHeapCommitted));
            lst.add(new MemoryInfoItem("None Heap Max", noneHeapMax));
            lst.add(new MemoryInfoItem("None Heap Init", noneHeapInit));
            return lst;
        }

        @Override
        public String toString() {
            return "Free Physical Memory Size: " + freePhysicalMemorySize + " ("
                    + Utils.formatFileSize(freePhysicalMemorySize) + ")\n"
                    + "Total Physical Memory Size: " + totalPhysicalMemorySize + " ("
                    + Utils.formatFileSize(totalPhysicalMemorySize) + ")\n"
                    + "Free Swap Space Size: " + freeSwapSpaceSize + " ("
                    + Utils.formatFileSize(freeSwapSpaceSize) + ")\n"
                    + "Total Swap Space Size: " + totalSwapSpaceSize + " ("
                    + Utils.formatFileSize(totalSwapSpaceSize) + ")\n"
                    + "Heap Used: " + heapUsed + " (" + Utils.formatFileSize(heapUsed) + ")\n"
                    + "Heap Committed: " + heapCommitted + " (" + Utils.formatFileSize(heapCommitted) + ")\n"
                    + "Heap Max: " + heapMax + " (" + Utils.formatFileSize(heapMax) + ")\n"
                    + "Heap Init: " + heapInit + " (" + Utils.formatFileSize(heapInit) + ")\n"
                    + "None Heap Used: " + noneHeapUsed + " (" + Utils.formatFileSize(noneHeapUsed) + ")\n"
                    + "None Heap Committed: " + noneHeapCommitted + " (" + Utils.formatFileSize(noneHeapCommitted) + ")\n"
                    + "None Heap Max: " + noneHeapMax + " (" + Utils.formatFileSize(noneHeapMax) + ")\n"
                    + "None Heap Init: " + noneHeapInit + " (" + Utils.formatFileSize(noneHeapInit) + ")\n";
        }
    }

    public static String getProcName(int level) {

        StackTraceElement[] ste = Thread.currentThread().getStackTrace();

        if (level < 0)
            level = 0;
        if (level >= ste.length)
            level = ste.length - 1;
        return ste[level].getMethodName();
    }

    public static byte[] intToBytes(int i) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(i);
        return b.array();
    }

    public static Boolean strBool(String s, Boolean defValue) {
        if (s == null)
            return defValue;
        switch (s.toLowerCase()) {
            case "t":
            case "1":
            case "true":
            case "tak":
            case "y":
            case "yes":
            case "on":
                return true;

            case "f":
            case "0":
            case "false":
            case "nie":
            case "n":
            case "no":
            case "off":
                return false;
            default:
                return defValue;
        }
    }

    public static boolean isInArray(int[] array, int val) {

        for (int i : array)
            if (i == val)
                return true;
        return false;
    }

    public static int[] addToArray(int[] array, int val) {

        int size = array == null ? 0 : array.length;
        int[] arr = new int[size + 1];
        for (int i = 0; i < size; i++)
            arr[i] = array[i];
        arr[size] = val;
        return arr;
    }

    public static boolean checkId(String source, boolean raiseException) {
        return checkId(source, raiseException, null);
    }

    public static boolean checkId(String source, boolean raiseException, String extraChars) {
        String chars = "0123456789_abcdefghijklmnopqrstuwvxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (extraChars != null)
            chars += extraChars;

        if (source == null || source.trim().isEmpty())
            if (raiseException)
                throw new RuntimeException("Wartość nie może być pusta");
            else
                return false;

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            int idx = chars.indexOf(c);

            if (idx < 0 || (i == 0 && idx < 9))
                if (raiseException)
                    throw new RuntimeException("Niedozwolony znak (\"" + c
                            + "\", poz. " + (i + 1) + ") w wartości \""
                            + source + "\"");
                else
                    return false;

        }

        return true;
    }

    public static boolean addToStringListUnique(String str, List<String> lst) {
        if (str == null || lst == null)
            return false;
        for (String s : lst)
            if (s != null && s.equalsIgnoreCase(str))
                return false;
        lst.add(str);
        return true;
    }

    /**
     * Wymuszone parsowanie integera. Wszystkie nieprawidłowe znaki zostaną
     * zignorowane. Jesli wystąpi znak separatora dziesiętnego, cześć dziesiętna
     * zostanie obcięta
     */
    public static Integer strIntForce(String s, Integer def) {
        if (s == null || s.trim().isEmpty())
            return def;
        Integer result = def;
        try {

            String ss = "";
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '.' || c == ',')
                    break;
                if (c < '0' || c > '9')
                    continue;
                ss += c;
            }

            result = Integer.parseInt(ss);
        } catch (Exception ex) {
        }
        return result;
    }

    public static Integer strInt(String s, Integer def) {
        Integer result = def;
        try {
            s = s.replace(" ", "");
            result = Integer.parseInt(s);
        } catch (Exception ex) {
        }
        return result;
    }

    public static Double strDouble(String s, Double def) {
        Double result = def;
        try {
            result = Double.parseDouble(s);
        } catch (Exception ex) {
        }
        return result;
    }

    public static int strInt(String s) {
        int result = -1;
        try {
            result = Integer.parseInt(s);
        } catch (Exception ex) {
        }
        return result;
    }

    public static String boolToStr(Boolean b) {
        return b == null ? "" : b ? "Tak" : "Nie";
    }

    public static String formatFloat(double value) {

        if (value > 100)
            value = (double) Math.round(value);
        else
            if (value > 10)
                value = (double) Math.round(value * 10) / 10;
            else
                value = (double) Math.round(value * 100) / 100;

        int io = (int) Math.round(value);

        if (io == value)
            return formatValue(io);

        return formatValue(value);
    }

    /**
     * Dzieli wartość liczbową na bloki dodając spacje
     */
    public static String formatValue(Number value) {
        if (value == null)
            return null;

        String s = new DecimalFormat("#.#########################")
                .format(value)
                .replace(",", ".");

        String t = "";
        if (s.contains(".")) {
            t = s.substring(s.indexOf(".") + 1);
            s = s.substring(0, s.indexOf("."));
        }

        String ss = "";
        for (int i = 0; i < s.length(); i++) {
            ss = s.charAt(s.length() - i - 1) + ss;
            if (((i + 1) % 3) == 0)
                ss = " " + ss;
        }

        if (!t.isEmpty()) {
            ss += ".";
            for (int i = 0; i < t.length(); i++) {
                ss += t.charAt(i);
                if ((i + 1) % 3 == 0)
                    ss += " ";
            }
        }

        return ss.trim();
    }

    public static String formatFileSize(final long value) {

        double out;
        double val = Math.abs(value);
        String unit;
        String prefix = value < 0 ? "-" : "";

        if (val < 1024) {
            out = val;
            unit = " B";
        } else
            if (val < 0x100000) {
                out = val / 1024;
                unit = " KB";
            } else
                if (val < 0x40000000) {
                    out = val / 0x100000;
                    unit = " MB";
                } else {
                    out = val / 0x40000000;
                    unit = " GB";
                }

        if (out > 100)
            out = (double) Math.round(out);
        else
            if (out > 10)
                out = (double) Math.round(out * 10) / 10;
            else
                out = (double) Math.round(out * 100) / 100;

        int io = (int) Math.round(out);

        if (io == out)
            return io + unit;

        return prefix + Double.toString(out) + unit;
    }

    public static Object deserialize(byte[] data, String pass) {
        if (data == null)
            return null;
        byte[] buff = data;
        try {
            if (pass != null) {

                SecretKeySpec key = new SecretKeySpec(
                        MessageDigest.getInstance("MD5").digest(pass.getBytes()), "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, key);
                buff = cipher.doFinal(buff);

            }
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buff));
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (Exception ex) {
            return null;
        }
    }

    public static byte[] serialize(Object obj, String pass) {
        if (obj == null)
            return null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bout);
            out.writeObject(obj);
            out.close();

            if (pass == null)
                return bout.toByteArray();
            SecretKeySpec key = new SecretKeySpec(
                    MessageDigest.getInstance("MD5").digest(pass.getBytes()), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bout.toByteArray());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Losowe ustawianie elementow listy
     */
    public static void randomList(List<Object> list) {

        Random rand = new Random();
        List<Object> out = new LinkedList<>();

        while (!list.isEmpty()) {
            Object ob = list.get(rand.nextInt(list.size()));
            list.remove(ob);
            out.add(ob);
        }
        list.clear();
        for (Object ob : out)
            list.add(ob);

    }

    /**
     * Jeśli parametr jest nullem to zostanie zignorowany
     */
    public static String paramsToString(String separator, Object... elements) {
        if (elements == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (Object o : elements)
            if (o != null) {
                if (sb.length() > 0)
                    sb.append(separator);
                sb.append(o.toString());
            }
        return sb.toString();
    }

    public static String listToString(String separator, String... elements) {
        if (elements == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0)
                sb.append(separator);
            sb.append(elements[i]);
        }
        return sb.toString();
    }

    public static String listToString(Collection<? extends Object> lst) {
        return listToString(lst, ", ");
    }

    public static String listToString(Collection<? extends Object> lst, String separator) {
        if (lst == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (Object s : lst) {
            if (s == null)
                continue;
            if (sb.length() > 0)
                sb.append(separator);
            sb.append(s);
        }

        return sb.toString();
    }

    public static boolean tryStrToInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String cutLongName(String s, int length, boolean asFileName) {
        if (s == null)
            return s;
        s = s.trim();
        if (s.length() <= length)
            return s;
        return s.substring(0, length - 1).trim() + "… ";
    }

    /**
     * Czy dana klasa (child) dziedziczy po innej (superClasses)
     *
     * @param child
     * @param superClasses
     * @return
     */
    public static boolean classExtends(Class child, Class<?>... superClasses) {
        if (superClasses == null)
            return false;
        while (child != null) {
            for (Class<?> cls : superClasses)
                if (child == cls)
                    return true;
            child = child.getSuperclass();
        }
        return false;
    }

    public static boolean hasAsSuperClsss(Class superClass, Class child) {

        while (child != null) {
            if (child == superClass)
                return true;
            child = child.getSuperclass();
        }
        return false;
    }

    /**
     * Czy dana klasa istnieje na liscie wywolan. Procedura pomocna przy
     * eliminacji zapetlen
     */
    public static boolean isInStackTrace(Class cls) {
        String clsName = cls.getName();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace())
            if (ste.getClassName().startsWith(clsName))
                return true;

        return false;
    }

    public static boolean isInStackTrace(String method) {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace())
            if (method.equals(ste.getClassName() + "." + ste.getMethodName()))
                return true;
        return false;

    }
    //***********************************************************************************************
    // --------------------- performacne ---------------------------------
    public final static Map<String, PerformanceStamp> performaceStamps = new TreeMap<>();

    public static class PerformanceStamp {

        public final List<Long> times = new LinkedList<>();
        public Object data;
        public String description;

        public static PerformanceStamp add(String name) {
            synchronized (performaceStamps) {
                PerformanceStamp ps = performaceStamps.get(name);

                if (ps == null) {
                    ps = new PerformanceStamp();
                    performaceStamps.put(name, ps);
                }

                ps.times.add(new Date().getTime());

                if (performaceStamps.size() > 1000)
                    performaceStamps.remove(performaceStamps.keySet().iterator().next());

                return ps;
            }
        }

        private PerformanceStamp() {
        }
    }

    //===========================================================================================
    /**
     * Lista przesówana - bufor.
     */
    @SuppressWarnings("unchecked")
    public static class ShiftList<T extends Object> implements Iterable<T> {

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                public int itr = index;

                @Override
                public boolean hasNext() {
                    return itr < total && itr <= index;

                }

                @Override
                public T next() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void remove() {
                }
            };
        }

        public static interface IOverrideItem<T extends Object> {

            public void onOverrideItem(T item);
        }
        //-----------------------------------
        public IOverrideItem onOverrideItem;
        private final Object[] array;
        private int index = 0;
        private long total = 0;

        public <T extends Object> ShiftList(int size) {
            array = new Object[size];
        }

        public long size() {
            return total;
        }

        public List<T> getArray() {

            if (total <= array.length) {
                List<T> lst = new ArrayList<>();
                for (int i = 0; i < total; i++)
                    lst.add((T) array[i]);
                return lst;
            }

            int len = (int) (total >= array.length ? array.length : index);

            List<T> lst = new ArrayList<>();

            for (int i = index; i < len; i++)
                lst.add((T) array[i]);

            for (int i = 0; i < index; i++)
                lst.add((T) array[i]);

            return lst;
        }

        public void add(T... elements) {
            if (elements == null)
                return;
            for (T el : elements) {
                if (onOverrideItem != null && total > index)
                    onOverrideItem.onOverrideItem(array[index]);
                array[index] = el;
                ++index;
                ++total;
                if (index >= array.length)
                    index = 0;
            }

        }
    }

    public static String getThreadPriorityName(int priority) {

        if (priority <= 2)
            return "Najniższy";

        switch (priority) {
            case 3:
            case 4:
                return "Niski";
            case 5:
                return "Normalny";
            case 6:
            case 7:
                return "Wysoki";
            case 8:
            case 9:
                return "Najwyższy";
            case 10:
                return "Czasu rzeczywistego";
        }

        return "Nieznany";

    }

    public static String replace(String text, String from, Number to) {
        return replace(text, from, "" + to);
    }

    public static String replace(String text, String from, String to) {
        int pos;
        while ((pos = text.indexOf(from)) >= 0)
            text = text.substring(0, pos) + to
                    + text.substring(pos + from.length(), text.length());
        return text;
    }

    /**
     * Zwraca pierwszy argument, który nie jest NULLem
     *
     * @param arguments
     * @return
     */
    public static String coalesce(Object... arguments) {
        if (arguments != null)
            for (Object o : arguments)
                if (o != null)
                    return Utils.toString(o);
        return null;
    }

    public static String coalesceNonEmpty(Object... arguments) {
        if (arguments != null)
            for (Object o : arguments)
                if (o != null) {
                    String str = Utils.toString(o);
                    if (str != null && !str.isEmpty())
                        return str;
                }
        return null;
    }

    /**
     * Pobierz ścieżkę pliku jar na podstawie klasy
     *
     * @param cls
     * @return
     */
    public static String getJarPath(Class<?> cls) {
        String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            return URLDecoder.decode(path, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException ex) {
            return path;
        }
    }

}
