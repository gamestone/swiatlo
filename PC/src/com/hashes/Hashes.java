package com.hashes;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**

 @author Miłosz Ziernik
 */
public class Hashes {

    public static enum Hash {

        CRC32, MD5, SHA1, SHA256
    }

    public static byte[] md5B(String str) {
        return hashB(Hash.MD5, str.getBytes());
    }

    public static String md5(String str) {
        return hash(Hash.MD5, str);
    }

    public static String md5(byte[] data) {
        return hash(Hash.MD5, data);
    }

    public static String hash(Hash hash, File file) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return Hex.toString(hashB(hash, in));
        } finally {
            in.close();
        }
    }

    public static byte[] hashB(Hash hash, File file) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return hashB(hash, in);
        } finally {
            in.close();
        }
    }

    public static String hash(Hash hash, String str) {
        return Hex.toString(hashB(hash, str.getBytes()));
    }

    public static byte[] hashB(Hash hash, String str) {
        return hashB(hash, str.getBytes());
    }

    public static String hash(Hash hash, byte[] buff) {
        try {
            return Hex.toString(hashB(hash, new ByteArrayInputStream(buff)));
        } catch (IOException ex) {
            return "";
        }
    }

    public static byte[] hashB(Hash hash, byte[] buff) {
        try {
            return hashB(hash, new ByteArrayInputStream(buff));
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public static String hash(Hash hash, InputStream is) throws IOException {
        return Hex.toString(hashB(hash, is));
    }

    public static byte[] hashB(Hash hash, InputStream is) throws IOException {
        MessageDigest digest = null;
        CRC32 crc = null;
        try {
            switch (hash) {
                case CRC32: {
                    crc = new CRC32();
                    break;
                }
                case MD5: {
                    digest = MessageDigest.getInstance("MD5");
                    break;
                }
                case SHA1: {
                    digest = MessageDigest.getInstance("SHA-1");
                    break;
                }
                case SHA256: {
                    digest = MessageDigest.getInstance("SHA-256");
                    break;
                }
            }

            byte[] buffer = new byte[8192];
            int read;

            if (crc != null) {
                while ((read = is.read(buffer)) > 0)
                    crc.update(buffer, 0, read);
                return ByteBuffer.allocate(4).putInt((int) crc.getValue()).array();
            }

            if (digest != null) {
                while ((read = is.read(buffer)) > 0)
                    digest.update(buffer, 0, read);
                return digest.digest();
            }

            return new byte[0];

        } catch (NoSuchAlgorithmException ex) {
            return new byte[0];
        }
    }

    public static class HashOutputStream extends OutputStream {

        private final OutputStream out;
        private MessageDigest digest;
        private byte[] res;
        private long length = 0;
        private CRC32 crc;
        public boolean enabled = true;

        public HashOutputStream(Hash hash, OutputStream out) {
            this.out = out;
            try {
                switch (hash) {
                    case CRC32: {
                        crc = new CRC32();
                        break;
                    }
                    case MD5: {
                        digest = MessageDigest.getInstance("MD5");
                        break;
                    }
                    case SHA1: {
                        digest = MessageDigest.getInstance("SHA-1");
                        break;
                    }
                    case SHA256: {
                        digest = MessageDigest.getInstance("SHA-256");
                        break;
                    }
                }
            } catch (NoSuchAlgorithmException ex) {
            }
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            ++length;
            if (!enabled)
                return;
            byte bb = (byte) (b & 0xFF);
            if (crc != null)
                crc.update(bb);
            else
                digest.update(bb);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        public long getLength() {
            return length;
        }

        public byte[] getAsBytes() {
            if (length == 0)
                return new byte[0];

            if (res == null)
                res = (crc != null
                       ? ByteBuffer.allocate(4).putInt((int) crc.getValue()).array()
                       : digest != null ? digest.digest() : new byte[0]);
            return res;
        }

        public int getCRC32() {
            if (crc == null)
                return 0;
            return (int) crc.getValue();
        }

        public String getAsString() {
            return Hex.toString(getAsBytes());
        }
    }

    public static class HashInputStream extends InputStream {

        private final InputStream in;
        private MessageDigest digest;
        private byte[] res;
        private long length = 0;
        private CRC32 crc;
        public boolean enabled = true;

        public HashInputStream(Hash hash, InputStream in) {
            this.in = in;
            try {
                switch (hash) {
                    case CRC32: {
                        crc = new CRC32();
                        break;
                    }
                    case MD5: {
                        digest = MessageDigest.getInstance("MD5");
                        break;
                    }
                    case SHA1: {
                        digest = MessageDigest.getInstance("SHA-1");
                        break;
                    }
                    case SHA256: {
                        digest = MessageDigest.getInstance("SHA-256");
                        break;
                    }
                }
            } catch (NoSuchAlgorithmException ex) {
            }
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public final int read() throws IOException {
            int v = in.read();
            ++length;
            if (!enabled)
                return v;
            if (crc != null && v != -1)
                crc.update((byte) (v & 0xFF));
            else
                if (digest != null && v != -1)
                    digest.update((byte) (v & 0xFF));
            return v;
        }

        public long getLength() {
            return length;
        }

        public int getCRC32() {
            if (crc == null)
                return 0;
            return (int) crc.getValue();
        }

        public byte[] getAsBytes() {
            if (length == 0)
                return new byte[0];

            if (res == null)
                res = (crc != null
                       ? ByteBuffer.allocate(4).putInt((int) crc.getValue()).array()
                       : digest != null ? digest.digest() : new byte[0]);
            return res;
        }

        public String getAsString() {
            return Hex.toString(getAsBytes());
        }
    }
}
