package com.io;

import java.io.*;

//--------------------------------------------------------------------------
/**
 InputStream z możliwością zapisu do strumieni wyjściowych
 */
public class InputToOutputStream extends InputStream implements Closeable {

    private final InputStream in;
    private final OutputStream[] out;
    private long count;

    public InputToOutputStream(InputStream in, OutputStream... out) {
        this.in = in;
        this.out = out;
    }

    public long count() {
        return count;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public int read() throws IOException {
        int v = in.read();

        if (v != -1 && out != null)
            for (OutputStream os : out)
                os.write(v);
        ++count;
        return v;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public long readDynamicLength() throws IOException {
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
