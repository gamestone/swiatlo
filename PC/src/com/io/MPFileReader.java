package com.io;

import com.io.MPFileReader.Part;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 * Miłosz Ziernik
 * 2013/11/14 
 */
public class MPFileReader implements Iterable<Part> {

    public int bufferSizeKB = 100; // rozmiar bufora [KB]
    public final byte[] extra;
    private byte[] cryptKey;
    public final Date date;
    private final File file;

    private final List<Part> parts = new LinkedList<>();

    @Override
    public Iterator<Part> iterator() {
        return parts.iterator();
    }

    public int getPartCount() {
        return parts.size();
    }

    public Part getPart(int index) {
        return index >= 0 && index < parts.size() ? parts.get(index) : null;
    }

    public MPFileReader(File file, String signature) throws IOException {
        this.file = file;
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), 1024 * 100);
        try {
            int size = in.available();
            int v = in.read();

            if (v < 0 || v > 255)
                throw new IOException("Nieprawidłowa sygnatura pliku");

            byte[] bSign = new byte[v];
            in.read(bSign);

            if (signature != null
                    && !signature.isEmpty()
                    && !signature.equals(new String(bSign)))
                throw new IOException("Nieprawidłowa sygnatura pliku");

            v = in.read();
            if (v != 1)
                throw new IOException("Nieprawidłowa wersja pliku");

            int flags = in.read();

            int totalSize = readInt(in);
            if (totalSize > size)
                throw new IOException("Nieprawidłowy rozmiar pliku");

            int headerSize = readInt(in);

            if ((flags & 0x01) > 0) {
                v = readInt(in);
                extra = new byte[v];
                in.read(extra);
            } else
                extra = new byte[0];

            date = (flags & 0x04) > 0 ? new Date(readLong(in)) : null;

            int partsCount = in.read();
            if (partsCount < 0 || partsCount > 255)
                throw new IOException("Nieprawidłowa liczba części");

            int partOffset = headerSize;

            for (int i = 0; i < partsCount; i++) {
                Part part = new Part();
                part.offset = partOffset;
                byte f = (byte) in.read();

                part.encrypted = (f & 0x04) > 0;
                part.compressed = (f & 0x08) > 0;

                if ((f & 0x01) > 0)
                    part.type = readInt(in);

                if ((f & 0x02) > 0) {
                    v = readInt(in);
                    if (v < 0 || v > 1000000)
                        throw new IOException();
                    byte[] buff = new byte[v];
                    in.read(buff);
                    part.name = new String(buff, "UTF-8");
                }

                if ((f & 0x10) > 0) {
                    v = readInt(in);
                    if (v < 0 || v > 1024 * 1024 * 10)
                        throw new IOException();
                    part.extra = new byte[v];
                    in.read(part.extra);
                }

                if ((f & 0x20) > 0) {
                    part.md5 = new byte[16];
                    in.read(part.md5);
                }

                if ((f & 0x40) > 0) {
                    part.passHash = new byte[16];
                    in.read(part.passHash);
                }
                part.size = readInt(in);
                part.oryginalSize = readInt(in);
                if (part.encrypted)
                    part.cipherPadding = (byte) in.read();
                partOffset += part.size;

                parts.add(part);
            }
        } finally {
            in.close();
        }
    }

    public class Part {

        public Integer type;
        public String name;
        public int size;
        public int oryginalSize;
        public boolean encrypted;
        public byte[] extra;
        public byte[] md5;
        private byte[] passHash;
        private int offset;
        public boolean compressed;
        private byte cipherPadding;
        private Cipher cipher;
        private MessageDigest digest;
        public boolean verifyChecksum = true;
        private byte[] cryptKey = MPFileReader.this.cryptKey;

        private class Decompressor extends InflaterInputStream {

            public Decompressor(InputStream in) {
                super(in);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int v = super.read(b, off, len);
                if (v > 0 && digest != null && compressed)
                    digest.update(b, 0, v);
                return v;
            }

            @Override
            public int read() throws IOException {
                int v = super.read();
                if (digest != null && !compressed)
                    digest.update((byte) (v & 0xFF));
                return v & 0xFF;
            }

        }

        public Part setDecryptKey(String key) {
            try {
                cryptKey = key != null ? MessageDigest.getInstance("MD5")
                        .digest(key.getBytes()) : null;
            } catch (NoSuchAlgorithmException ex) {
                cryptKey = null;
            }
            return this;
        }

        public InputStream getInputStream() throws IOException {
            if (encrypted) {

                if (cryptKey == null)
                    throw new IOException("Brak klucza");

                if (passHash != null)
                    try {
                        byte[] buff = Arrays.copyOf(cryptKey, cryptKey.length);
                        for (int i = 0; i < buff.length; i++)
                            buff[i] = (byte) (buff[i] ^ (~i - 0xA6));

                        buff = MessageDigest.getInstance("MD5").digest(buff);

                        if (!Arrays.equals(buff, passHash))
                            throw new IOException("Nieprawidłowy klucz");

                    } catch (NoSuchAlgorithmException ex) {
                        throw new IOException(ex);
                    }

                try {
                    if (cryptKey == null)
                        throw new IOException("Brak klucza");
                    cipher = Cipher.getInstance("AES/ECB/NoPadding");
                    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cryptKey, "AES"));
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
                    throw new IOException(ex);
                }
            }

            if (md5 != null)
                try {
                    digest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException ex) {
                    throw new IOException(ex);
                }

            MPFInputStream mis = new MPFInputStream();

            if (compressed)
                return new Decompressor(mis);

            return mis;
        }

        public byte[] getData() throws IOException {
            InputStream in = getInputStream();
            try {
                byte[] buff = new byte[bufferSizeKB * 1024]; // Adjust if you want
                int v;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while ((v = in.read(buff)) != -1)
                    bout.write(buff, 0, v);
                return bout.toByteArray();
            } finally {
                in.close();
            }

        }

        public class MPFInputStream extends InputStream {

            byte[] buffer = new byte[0];
            int buffPos = 0;
            int totalPos = 0;
            private final BufferedInputStream in;

            public MPFInputStream() throws IOException {
                in = new BufferedInputStream(new FileInputStream(file), 1024 * 100);
                long seek = Part.this.offset + totalPos;
                in.skip(seek);
            }

            @Override
            public int available() {
                return oryginalSize - totalPos;
            }

            @Override
            public int read() throws IOException {
                if (buffPos >= buffer.length)
                    readBuffer();

                if (buffer == null || buffer.length == 0 || totalPos >= size - cipherPadding)
                    return -1;

                ++totalPos;
                int i = (buffer[buffPos++] & 0xFF);

                if (digest != null && !compressed)
                    digest.update((byte) (i & 0xFF));

                return i;
            }

            private void readBuffer() throws IOException {
                int count = size - totalPos;

                if (count <= 0) {
                    buffer = new byte[0];
                    return;
                }

                if (count > bufferSizeKB * 1024)
                    count = bufferSizeKB * 1024;

                buffer = new byte[count];

                in.read(buffer);

                if (cipher != null)
                    try {
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();

                        CipherInputStream cin = new CipherInputStream(
                                new ByteArrayInputStream(buffer), cipher);
                        try {
                            byte[] buff = new byte[bufferSizeKB * 1024]; // Adjust if you want
                            int v;
                            while ((v = cin.read(buff)) != -1)
                                bout.write(buff, 0, v);
                        } finally {
                            cin.close();
                        }
                        buffer = bout.toByteArray();
                    } catch (Exception e) {
                        throw new IOException(e);
                    }

                buffPos = 0;
            }

            @Override
            public void close() throws IOException {
                in.close();
                super.close();
                if (digest == null || !verifyChecksum)
                    return;

                byte[] bmd5 = digest.digest();

                if (!Arrays.equals(md5, bmd5))
                    throw new IOException("Nieprawidłowa suma kontrolna pliku");

            }
        }
    }

    private int readInt(InputStream in) throws IOException {
        byte[] buff = new byte[4];
        in.read(buff);
        return ByteBuffer.wrap(buff).getInt();
    }

    private long readLong(InputStream in) throws IOException {
        byte[] buff = new byte[8];
        in.read(buff);
        return ByteBuffer.wrap(buff).getLong();
    }
}
