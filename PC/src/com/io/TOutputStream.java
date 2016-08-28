package com.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TOutputStream<TOut extends TOutputStream>
        extends OutputStream implements Closeable, Flushable {

    protected long length = 0;
    protected OutputStream out;
    private boolean inverseByteOrder = false;
    @SuppressWarnings("unchecked")
    private final TOut self = (TOut) this;
    private long startTime;
    private long endTime;

    public TOutputStream(final OutputStream out) {
        this.out = out;
    }

    public long getSpeed() {
        if (endTime == 0)
            endTime = System.currentTimeMillis();
        return (long) ((double) length / ((endTime - startTime) / 1000d));
    }

    public long getLength() {
        return length;
    }

    public TOut setInverseByteOrder(boolean value) {
        inverseByteOrder = value;
        return self;
    }

    public void copy(InputStream source, int length) throws IOException {
        IOUtils.copy(source, this, length, false);
    }

    public void copy(InputStream source) throws IOException {
        IOUtils.copy(source, this);
    }

    @Override
    public void write(int b) throws IOException {
        if (startTime == 0)
            startTime = System.currentTimeMillis();
        out.write(b);
        ++length;
    }

    public TOut writeByte(Byte value) throws IOException {
        if (value != null)
            write(value);
        return self;
    }

    public TOut writeShort(Short value) throws IOException {
        if (value != null)
            write(ByteBuffer.allocate(2)
                    .putShort(inverseByteOrder
                              ? Short.reverseBytes(value)
                              : value).array());
        return self;
    }

    public TOut writeInt(Integer value) throws IOException {
        if (value != null)
            write(ByteBuffer.allocate(4)
                    .putInt(inverseByteOrder
                            ? Integer.reverseBytes(value)
                            : value).array());
        return self;
    }

    public TOut writeLong(Long value) throws IOException {
        if (value != null)
            write(ByteBuffer.allocate(8)
                    .putLong(inverseByteOrder
                             ? Long.reverseBytes(value)
                             : value).array());
        return self;
    }

    public TOut writeFloat(Float value) throws IOException {
        if (value != null)
            write(ByteBuffer.allocate(4).putFloat(value).array());
        return self;
    }

    public TOut writeDouble(Double value) throws IOException {
        if (value != null)
            write(ByteBuffer.allocate(8).putDouble(value).array());
        return self;
    }

    public TOut writeString(String value, boolean includeLength) throws IOException {
        return writeString(value, Charset.forName("UTF-8"), includeLength);
    }

    public TOut writeStringDL(String value) throws IOException {
        byte[] buff = value.getBytes(Charset.forName("UTF-8"));
        writeDynamicLength(buff.length);
        write(buff);
        return self;
    }

    public TOut writeString(String value, Charset charset, boolean includeLength) throws IOException {
        if (value != null) {
            byte[] buff = value.getBytes(charset);
            if (includeLength)
                writeInt(buff.length);
            write(buff);
        }
        return self;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
        endTime = System.currentTimeMillis();
    }

    @Override
    public void close() throws IOException {
        out.close();
        endTime = System.currentTimeMillis();
    }

    public void writeDynamicLength(long value) throws IOException {
        if (value < 0)
            throw new IOException("Wartość " + value + " nie może być mniejsza niż 0");
        while (value > 0x70) {
            out.write(((byte) value | 0x80) & 0xFF);
            value = value >> 7;
        }
        out.write((byte) value);
    }
}
