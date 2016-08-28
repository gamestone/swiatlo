package com.io;

import com.hashes.Hashes;
import com.hashes.Hashes.Hash;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class MPFileWriter {

    public byte[] signature;
    public byte[] extra;

    private final List<Part> parts = new LinkedList<>();
    public String cryptKey;
    private final IOBuffer builder = new IOBuffer();
    public boolean saveMD5 = true;
    public boolean encrypted = false;
    public Date date = new Date();

    public Part add(Integer type, String name) {
        Part part = new Part();
        parts.add(part);
        part.name = name;
        part.type = type;
        return part;
    }

    private boolean builded = false;

    public IOBuffer build() throws IOException {
        if (builded)
            return builder;

        builded = true;
        try {
            if (signature != null) {
                builder.writeByte((byte) signature.length);
                builder.write(signature);
            }

            builder.writeByte((byte) 1); // wersja

            byte flags = 0;
            flags |= extra != null ? 0x01 : 0;
            flags |= date != null ? 0x02 : 0;
            flags |= 0x04; // naglowek na poczatku pliku

            builder.writeByte(flags);

            TOutputStream<TOutputStream> header = new TOutputStream<>(
                    new ByteArrayOutputStream());

            if (extra != null) {
                header.writeInt(extra.length);
                header.write(extra);
            }

            if (date != null)
                header.writeLong(date.getTime());

            header.writeByte((byte) parts.size());

            for (Part p : parts) {
                p.flush();

                byte f = 0;
                f += p.type != null ? 0x01 : 0;
                f += p.name != null ? 0x02 : 0;
                f += p.encrypted ? 0x04 : 0;
                f += p.compressed ? 0x08 : 0;
                f += p.extra != null ? 0x10 : 0;
                f += p.saveMD5 ? 0x20 : 0;
                f |= p.savePasswordHash && p.cryptKey != null ? 0x40 : 0;

                header.write(f);
                if (p.type != null)
                    header.writeInt(p.type);

                if (p.name != null) {
                    byte[] bname = p.name.getBytes("UTF-8");
                    header.writeInt(bname.length);
                    header.write(bname);
                }

                if (p.extra != null) {
                    header.writeInt(p.extra.length);
                    header.write(p.extra);
                }
                //     return out;
                if (p.digest != null)
                    header.write(p.digest.digest());

                if (p.savePasswordHash && p.cryptKey != null) {
                    byte[] bKey = Hashes.hashB(Hashes.Hash.MD5, p.cryptKey);
                    byte[] bPassHash = Arrays.copyOf(bKey, bKey.length);
                    for (int i = 0; i < bPassHash.length; i++)
                        bPassHash[i] = (byte) (bPassHash[i] ^ (~i - 0xA6));
                    bPassHash = Hashes.hashB(Hashes.Hash.MD5, bPassHash);
                    header.write(bPassHash);
                }

                header.writeInt((int) p.buff.length());   // rozmiar rzeczywisty
                header.writeInt(p.writed);  // rozmiar oryginalny
                if (p.encrypted)
                    header.writeByte((byte) p.cipherPadding);
            }

            int total = 0;

            for (Part p : parts)
                total += p.buff.length();

            int hdrSize = (int) builder.length() + (int) header.length + 8;

            builder.writeInt(total + hdrSize); // rozmiar calkowity
            builder.writeInt(hdrSize);  // rozmiar naglowka

            builder.write(((ByteArrayOutputStream) header.out).toByteArray());

            for (Part p : parts)
                IOUtils.copy(p.buff.getInputStream(), builder);

            return builder;
        } finally {
            for (Part p : parts)
                p.buff.delete();
        }

    }

    public class Part extends TOutputStream<Part> {

        private CipherOut cipherOut;
        private DeflaterOut deflaterOut;
        private int cipherPadding;
        protected int writed = 0;
        public String cryptKey = MPFileWriter.this.cryptKey;
        public boolean savePasswordHash = true;

        private class DeflaterOut extends DeflaterOutputStream {

            private boolean enabled;

            public DeflaterOut(OutputStream out) {
                super(out);
            }

            @Override
            public void write(int b) throws IOException {
                if (enabled)
                    super.write(b);
                else
                    cipherOut.write(b);
                ++length;

            }
        }

        private class CipherOut extends CipherOutputStream {

            private int length;
            private boolean enabled;

            public CipherOut(OutputStream out, Cipher cipher) {
                super(out, cipher);
            }

            @Override
            public void write(int b) throws IOException {
                if (enabled)
                    super.write(b);
                else
                    buff.write(b);
                ++length;
            }

            @Override
            public void write(byte[] bytes) throws IOException {
                if (enabled)
                    super.write(bytes);
                else
                    buff.write(bytes);
                length += bytes.length;
            }

            @Override
            public void write(byte[] bytes, int i, int i1) throws IOException {
                if (enabled)
                    super.write(bytes, i, i1);
                else
                    buff.write(bytes, i, i1);
                length += i1 - i;
            }

        }

        public String name;
        public Integer type;
        public byte[] extra;
        public boolean saveMD5 = MPFileWriter.this.saveMD5;
        public boolean encrypted = MPFileWriter.this.encrypted;
        public boolean compressed;
        private MessageDigest digest;

        private final IOBuffer buff = new IOBuffer();

        public Part() {
            super(null);
        }

        private void initialize() throws IOException {

            buff.deleteOnClose = false;

            Cipher cipher = null;

            if (encrypted) {

                //int padding = in.available() % 16 != 0 ? 16 - in.available() % 16 : 0;
                try {
                    cipher = Cipher.getInstance("AES/ECB/NoPadding");
                    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(
                            Hashes.hashB(Hash.MD5, cryptKey), "AES"));

                } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    throw new IOException(e);
                }
            }

            cipherOut = new CipherOut(buff, cipher);
            deflaterOut = new DeflaterOut(cipherOut);

            cipherOut.enabled = encrypted;
            deflaterOut.enabled = compressed;

            if (saveMD5)
                try {
                    digest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException ex) {
                    throw new IOException(ex);
                }

            super.out = deflaterOut;
        }

        @Override
        public void write(int b) throws IOException {
            //     Log.debug("MPF Write", writed + " <" + (b & 0xFF) + "> '" + (char) b + "'");
            if (super.out == null)
                initialize();

            if (!flushed)
                ++writed;

            if (digest != null)
                digest.update((byte) b);

            super.write(b);
        }

        private boolean flushed;

        @Override
        public void flush() throws IOException {

            if (deflaterOut != null && deflaterOut.enabled) {
                deflaterOut.finish();
            }

            if (cipherOut != null && cipherOut.enabled && !flushed) {
                flushed = true;
                cipherPadding = cipherOut.length;
                cipherPadding = cipherPadding % 16 != 0 ? 16 - cipherPadding % 16 : 0;

                for (int i = 0; i < cipherPadding; i++) {
                    cipherOut.write(0);
                }

            }

            flushed = true;
            super.flush();
        }

    }
}
