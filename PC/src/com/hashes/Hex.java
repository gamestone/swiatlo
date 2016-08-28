package com.hashes;

public abstract class Hex {

    private static final char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String toString(byte[] buff) {
        return toString(buff, "");
    }

    public static String toString(byte[] buff, String separator) {
        return toString(buff, 0, buff.length, separator);
    }

    public static String toString(long value) {
        return toString(value, "");
    }

    public static String toString(long value, String separator) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[7 - i] = (byte) (value >>> (i * 8));
        }
        return Hex.toString(b, separator);
    }

    public static String toString(long value, int length) {
        return toString(value, length, "");
    }

    public static String toString(long value, int length, String separator) {
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[(length - 1) - i] = (byte) (value >>> (i * 8));
        }
        return Hex.toString(b, separator);
    }

    public static String toString(byte value) {
        return toString(value, "");
    }

    public static String toString(byte value, String separator) {
        byte[] b = new byte[1];
        b[0] = value;
        return Hex.toString(b, separator);
    }

    public static String toString(int value) {
        return toString(value, "");
    }

    public static String toString(int value, String separator) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[3 - i] = (byte) (value >>> (i * 8));
        }
        return Hex.toString(b, separator);
    }

    public static long toLong(String hex) {
        byte[] buff = toBytes(hex);

        long l = 0;
        for (int i = 0; i < buff.length; i++) {
            l <<= 8; // this is the same as l = l << 8;  
            l |= buff[i] & 0xFF;
        }
        return l;
    }

    public static int toInt(String hex) {
        byte[] buff = toBytes(hex);

        int l = 0;
        for (int i = 0; i < buff.length; i++) {
            l <<= 8; // this is the same as l = l << 8;  
            l |= buff[i] & 0xFF;
        }
        return l;
    }

    public static String toString(byte in[], int start, int length, String separator) {
        String asHexString = null;
        if (in != null) {
            StringBuilder out = new StringBuilder(in.length * 2);
            for (int x = start; x < length; x++) {
                int nybble = in[x] & 0xF0;
                nybble = nybble >>> 4;
                out.append(hexChars[nybble]);
                out.append(hexChars[in[x] & 0x0F]);
                if (x < length - 1)
                    out.append(separator);
            }
            asHexString = out.toString();
        }
        return asHexString;
    }

    public static byte[] toBytes(String hexString) {

        StringBuilder sb = new StringBuilder(hexString.length());
        for (int x = 0; x < hexString.length(); x++) {
            char c = Character.toLowerCase(hexString.charAt(x));

            if (c <= 32)
                continue;

            if (isHexChar(c)) {
                sb.append(c);
            }
            else
                if (!Character.isWhitespace(c)) {
                    throw new IllegalStateException(String.format(
                            "Conversion of hex string to array failed. '%c' is "
                            + "not a valid hex character", c));
                }
        }

        if (sb.length() % 2 > 0) {
            sb.append('0');
        }
        byte[] hexArray = new byte[hexString.length() + 1 >> 1];
        for (int x = 0; x < hexArray.length; x++) {
            int ni = x << 1;

            int mostSignificantNybble = Character.digit(sb.charAt(ni), 16);
            int leastSignificantNybble = Character.digit(sb.charAt(ni + 1), 16);

            int value = ((mostSignificantNybble << 4)) | (leastSignificantNybble & 0x0F);
            hexArray[x] = (byte) value;
        }
        return hexArray;
    }

    public static boolean isHexChar(char current) {
        return Character.digit(current, 16) >= 0;
    }
}
