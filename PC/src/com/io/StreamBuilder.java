package com.io;

import com.hashes.Hashes;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Miłosz Ziernik
 */
public class StreamBuilder {

    public String cryptKey = null; // jesli jest zdefiniowany, dane zostana zaszyfrowane
    public String cipherStr = "AES/ECB/PKCS5Padding";

    /* Nagłówek:
     * 1-4 - type id
     * 5 - skompresowany
     * 6 - zaszyfrowany
     * 7 - zawiera dane (nie pusty)
     * 8 > rozmiar
     */
    public static enum TypeId {
        //maksymalna wartość: 15

        unknown(0, 0, "Nieznany"),
        bool(1, 1, "Boolean"),
        byte_(2, 1, "Byte"),
        short_(3, 2, "Short"),
        integer(4, 4, "Integer"),
        float_(5, 4, "Float"),
        long_(6, 8, "Long"),
        double_(7, 8, "Double"),
        date(8, 8, "Date"),
        uid(9, 8, "UID"), // 8 bajtow
        dynamic(10, 0, "Dynamic Value"), // dyamiczna wartość całkowita
        string(11, 0, "String"),
        bytes(12, 0, "Byte Array"),
        object(13, 0, "Serialized Object");
        public final byte id;
        public final int size;
        public final String name;

        private TypeId(int id, int size, String name) {
            this.id = (byte) id;
            this.size = size;
            this.name = name;
        }
    }

    public static class StreamWriter extends StreamBuilder {

        public long minCompressSize = 512; // minimalny rozmiar danych, ktory zostanie poddany kompresji  (tryb automatyczny) 
        public final OutputStream out;

        public StreamWriter() {
            out = new ByteArrayOutputStream();
        }

        public StreamWriter(OutputStream out) {
            this.out = out;
        }

        public byte[] getData() throws IOException {
            if (!(out instanceof ByteArrayOutputStream))
                throw new IOException();
            return ((ByteArrayOutputStream) out).toByteArray();
        }

        public StreamWriter write(Boolean value) throws IOException {
            writeHeader(TypeId.bool, value == null ? 0 : 1, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(1).put(value ? (byte) 1 : 0).array());
            return this;
        }

        public StreamWriter write(Byte value) throws IOException {
            writeHeader(TypeId.byte_, value == null ? 0 : 1, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(1).put(value).array());
            return this;
        }

        public StreamWriter write(Short value) throws IOException {
            writeHeader(TypeId.short_, value == null ? 0 : 2, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(2).putShort(value).array());
            return this;
        }

        public StreamWriter write(Integer value) throws IOException {
            writeHeader(TypeId.integer, value == null ? 0 : 4, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(4).putInt(value).array());
            return this;
        }

        public StreamWriter write(Long value) throws IOException {
            writeHeader(TypeId.long_, value == null ? 0 : 8, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(8).putLong(value).array());
            return this;
        }

        public StreamWriter write(Float value) throws IOException {
            writeHeader(TypeId.float_, value == null ? 0 : 4, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(4).putFloat(value).array());
            return this;
        }

        public StreamWriter write(Double value) throws IOException {
            writeHeader(TypeId.double_, value == null ? 0 : 8, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(8).putDouble(value).array());
            return this;
        }

        public StreamWriter writeDynamic(long value) throws IOException {
            byte[] val = getDynamicLength(value);
            writeHeader(TypeId.dynamic, val.length, false, false);
            out.write(val);
            return this;
        }

        public StreamWriter write(Date value) throws IOException {
            writeHeader(TypeId.date, value == null ? 0 : 8, false, false);
            if (value != null)
                out.write(ByteBuffer.allocate(8).putLong(value.getTime()).array());
            return this;
        }

        public StreamWriter write(UUID value) throws IOException {
            writeHeader(TypeId.uid, value == null ? 0 : 8, false, false);
            if (value != null) {
                byte[] buff = new byte[16];
                ByteBuffer bb = ByteBuffer.wrap(buff);
                bb.putLong(value.getMostSignificantBits());
                bb.putLong(value.getLeastSignificantBits());
                out.write(buff);
            }
            return this;
        }

        public StreamWriter write(byte[] value, TypeId type, Boolean compress, String cryptKey)
                throws IOException {
            if (cryptKey == null)
                cryptKey = this.cryptKey;
            boolean compr = compress != null ? compress
                    : value != null && value.length >= minCompressSize;
            boolean crypt = cryptKey != null;
            long oryginalSize = value.length;

            //     new String(value);
            byte[] md5 = null;
            if (crypt)
                md5 = Hashes.hashB(Hashes.Hash.MD5, value);

            if (compr)
                value = compress(value);

            if (crypt)
                try {
                    Cipher cipher = Cipher.getInstance(cipherStr);
                    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(
                            MessageDigest.getInstance("MD5").digest(
                                    cryptKey.getBytes("UTF-8")), "AES"));
                    value = cipher.doFinal(value);
                } catch (Exception ex) {
                    throw new SCryptException(ex);
                }

            ByteArrayOutputStream bHdr = new ByteArrayOutputStream();
            if (crypt || compr) {
                writeDynLen(oryginalSize, bHdr);
                if (md5 != null)
                    bHdr.write(md5);
            }
            byte[] hdr = bHdr.toByteArray();

            writeHeader(type, hdr.length + value.length, compr, crypt);
            out.write(hdr);
            out.write(value);
            return this;
        }

        public StreamWriter write(byte[] value, Boolean compress, String cryptKey) throws IOException {
            return write(value, TypeId.bytes, compress, cryptKey);
        }

        public StreamWriter write(byte[] value) throws IOException {
            return write(value, null, null);
        }

        public StreamWriter write(String value, Boolean compress, String cryptKey) throws IOException {
            return write(value != null ? value.getBytes("UTF-8") : new byte[0],
                    TypeId.string, compress, cryptKey);
        }

        public StreamWriter write(String value) throws IOException {
            return write(value, null, null);
        }

        public StreamWriter writeSerialized(Serializable value, Boolean compress, String cryptKey) throws IOException {
            if (value == null) {
                writeHeader(TypeId.object, 0, false, false);
                return this;
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (ObjectOutput out = new ObjectOutputStream(bout)) {
                out.writeObject(value);
            }
            write(bout.toByteArray(), TypeId.object, compress, cryptKey);
            return this;
        }

        public StreamWriter writeSerialized(Serializable value) throws IOException {
            return writeSerialized(value, null, null);
        }

        private void writeHeader(TypeId type, long length, boolean compressed,
                boolean crypted) throws IOException {
            writeHeader(type, length, compressed, crypted, out);
        }

        private static void writeHeader(TypeId type, long length, boolean compressed,
                boolean crypted, OutputStream out) throws IOException {

            long val = (type.size > 0 ? 0 : length << 7)
                    | (type.id << 3)
                    | ((length == 0 ? 1 : 0) << 2)
                    | ((compressed ? 1 : 0) << 1)
                    | ((crypted ? 1 : 0));
            if (length > 0 && type.size <= 0)
                writeDynLen(val, out);
            else
                out.write((byte) val);
        }

        private static byte[] getDynamicLength(long value) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            writeDynLen(value, bout);
            return bout.toByteArray();
        }

        private static void writeDynLen(long value, OutputStream out) throws IOException {
            if (value < 0)
                throw new IOException("Wartość " + value + " nie może być mniejsza niż 0");
            while (value > 0x70) {
                out.write(((byte) value | 0x80) & 0xFF);
                value = value >> 7;
            }
            out.write((byte) value);
        }

        public void flush() throws IOException {
            out.flush();
        }

        public void close() throws IOException {
            out.close();
        }

        /**
         * Rozpoznaje typ obiektu i zapisuje go. Jeśli typ nie zostanie
         * określony, zapisuje jako string
         */
        public void writeObject(Object val) throws IOException {
            if (val == null)
                return;
            if (val instanceof Boolean)
                write((Boolean) val);
            else
                if (val instanceof Byte)
                    write((Byte) val);
                else
                    if (val instanceof Short)
                        write((Short) val);
                    else
                        if (val instanceof Integer)
                            write((Integer) val);
                        else
                            if (val instanceof Long)
                                write((Long) val);
                            else
                                if (val instanceof Float)
                                    write((Float) val);
                                else
                                    if (val instanceof Double)
                                        write((Double) val);
                                    else
                                        if (val instanceof Date)
                                            write((Date) val);
                                        else
                                            if (val instanceof UUID)
                                                write((UUID) val);
                                            else
                                                if (val instanceof byte[])
                                                    write((byte[]) val);
                                                else
                                                    write(val.toString());
        }
    }

    public static class SCryptException extends IOException {

        public SCryptException(String message) {
            super(message);
        }

        public SCryptException(Throwable e) {
            super(e);
        }
    }

    public static class StreamReader extends StreamBuilder implements Closeable {

        public final InputStream in;

        @Override
        public void close() throws IOException {
            in.close();
        }

        public static class SBlock {

            private final InputStream in;
            private final StreamBuilder sb;
            public final TypeId type;
            public final long size;
            public final boolean compressed;
            public final boolean crypted;
            public final boolean empty;
            public final long oryginalSize;

            private SBlock(StreamBuilder sb, InputStream in) throws IOException {
                this.in = in;
                this.sb = sb;
                TypeId fType = TypeId.unknown;
                long val = readDynamicLength(in);

                crypted = (val & 1) == 1;
                compressed = (val & 2) == 2;
                empty = (val & 4) == 4;

                byte bType = (byte) (val >> 3 & 0x0F);

                for (TypeId ti : TypeId.values())
                    if (ti.id == bType) {
                        fType = ti;
                        break;
                    }

                this.type = fType;
                long fSize = empty ? 0 : fType.size;

                if (!empty && fType.size <= 0)
                    fSize = val >> 7;

                int span = 0;
                if (compressed || crypted) {
                    int av = in.available();
                    oryginalSize = readDynamicLength(in);
                    span = av - in.available();
                } else
                    oryginalSize = fSize;

                size = fSize - span;

            }

            @Override
            public String toString() {
                return (type != null ? type.name + ", " : "") + size;
            }

            public Object getValue() throws IOException {
                return getValue(in, null);
            }

            public Object getValue(InputStream in, String cryptKey) throws IOException {

                byte[] md5 = new byte[0];
                if (crypted) {
                    md5 = new byte[16];
                    in.read(md5);
                }

                byte[] data = copy(in, size - md5.length, false);

                if (crypted) {
                    if (cryptKey == null)
                        cryptKey = sb.cryptKey;
                    if (cryptKey == null)
                        throw new SCryptException("Brak zdefiniowanego hasła");

                    try {
                        Cipher cipher = Cipher.getInstance(sb.cipherStr);
                        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(
                                MessageDigest.getInstance("MD5").digest(
                                        cryptKey.getBytes("UTF-8")), "AES"));
                        data = cipher.doFinal(data);
                    } catch (Exception e) {
                        throw new SCryptException(e);
                    }
                }

                if (compressed)
                    data = decompress(data);

                if (crypted)
                    if (!Arrays.equals(md5, Hashes.hashB(Hashes.Hash.MD5, data)))
                        throw new SCryptException("Błąd weryfikacji sumy kontrolnej");

                switch (type) {
                    case bool:
                        return data[0] == 1;
                    case byte_:
                        return data[0];
                    case short_:
                        return ByteBuffer.wrap(data).getShort();
                    case integer:
                        return ByteBuffer.wrap(data).getInt();
                    case long_:
                        return ByteBuffer.wrap(data).getLong();
                    case float_:
                        return ByteBuffer.wrap(data).getFloat();
                    case double_:
                        return ByteBuffer.wrap(data).getDouble();
                    case date:
                        return new Date(ByteBuffer.wrap(data).getLong());
                    case string:
                        return new String(data, "UTF-8");
                }
                return data;
            }

            /**
             * Pomiń bieżący blok
             */
            public void skip() throws IOException {
                in.skip(size);
            }
        }

        public StreamReader(InputStream in) {
            this.in = in;
        }

        public SBlock next() throws IOException {
            return next(null);
        }

        public SBlock next(String cryptKey) throws IOException {
            if (in.available() == 0)
                return null;
            return new SBlock(this, in);
        }

        public static long readDynamicLength(InputStream in) throws IOException {
            long value = 0;
            int shift = 0;
            while (in.available() > 0 || shift > 7) {
                int v = in.read();
                if (v == -1)
                    break;
                value |= (v & 0x7f) << shift;
                shift += 7;
                if (v < 0x80)
                    break;
            }
            if (value < 0)
                throw new IOException("Wartość " + value + " mniejsza niż 0");
            return value;
        }
    }

    //------------------------------------------------
    public static byte[] copy(InputStream in, long maxLength,
            boolean closeStreams) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        copy(in, bout, maxLength, closeStreams);
        return bout.toByteArray();
    }

    public static long copy(InputStream in, OutputStream out, long maxLength,
            boolean closeStreams) throws IOException {
        long total = 0;
        byte[] buf = new byte[maxLength > 0 && maxLength < 10240
                ? (int) maxLength : 10240];
        try {
            while (true) {
                int max = buf.length;
                if (maxLength > 0 && total + max > maxLength)
                    max = (int) (maxLength - total);
                int len = in.read(buf, 0, max);
                if (len <= 0)
                    break;

                out.write(buf, 0, len);
                total += len;
                if (maxLength > 0 && total >= maxLength)
                    break;
            }

        } finally {
            if (closeStreams) {
                in.close();
                out.close();
            }
            return total;
        }
    }

    public static long compress(InputStream in, OutputStream out) throws IOException {
        try (OutputStream dOut = new DeflaterOutputStream(out)) {
            return copy(in, dOut, 0, false);
        }
    }

    public static long compress(byte[] in, OutputStream out) throws IOException {
        try (OutputStream dOut = new DeflaterOutputStream(out)) {
            dOut.write(in);
        }
        return in.length;
    }

    public static byte[] compress(byte[] in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (OutputStream dOut = new DeflaterOutputStream(bout)) {
            dOut.write(in);
        }
        return bout.toByteArray();
    }

    public static long decompress(InputStream in, OutputStream out) throws IOException {
        try (InflaterInputStream dIn = new InflaterInputStream(in)) {
            return copy(dIn, out, 0, false);
        }
    }

    public static byte[] decompress(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        decompress(in, bout);
        return bout.toByteArray();
    }

    public static long decompress(byte[] data, OutputStream out) throws IOException {
        return decompress(new ByteArrayInputStream(data), out);
    }

    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        decompress(new ByteArrayInputStream(data), bout);
        return bout.toByteArray();
    }
}
