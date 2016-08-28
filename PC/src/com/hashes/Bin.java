package com.hashes;

public class Bin {

    public static String toString(long value, String fullByteSeparator, String halfByteSeparator) {
        String val = Long.toBinaryString(value);

        StringBuilder sb = new StringBuilder();

        while ((sb.length() + val.length()) % 8 != 0)
            sb.append("0");

        sb.append(val);

        char[] arr = sb.toString().toCharArray();
        sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i > 0 && i % 8 == 0)
                sb.append(fullByteSeparator);

            if (i > 0 && i % 4 == 0)
                sb.append(halfByteSeparator);

            sb.append(arr[i]);
        }

        return sb.toString();
    }

    public static String toString(long value) {
        return toString(value, " ", " ");
    }

    public static String toString(long value, String fullByteSeparator) {
        return toString(value, fullByteSeparator, " ");
    }

}
