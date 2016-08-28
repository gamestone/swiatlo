package com.io;

import com.hashes.Hex;
import mlogger.Log;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class IOBuffer extends TOutputStream<IOBuffer> {

    public int memoryBufferSizeLimit = 1024 * 1024;
    private File file;
    private long wlength = 0;
    private long fwlength = 0;
    //  private boolean locked = true;
    private final byte[] wbuffer = new byte[1024 * 100];
    private ByteArrayOutputStream bout = new ByteArrayOutputStream();
    private int wpos = 0;
    private final Object sync = new Object();
    public boolean autoFlush = true;
    public int autoFlushThreshold = 0; // automatycznie wykonuj flush co okreslona ilosc bajtow
    private boolean isModified = false;
    public IWriteCallback writeCallback;
    public boolean deleteOnClose = true;
    private boolean deleted;

    public Hash hash;
    private MessageDigest digest;

    private final String CDataRemoved = "Dane zostały usunięte";

    public static enum Hash {

        MD5, SHA1, SHA256
    }

    public IOBuffer() {
        super(null);
    }

    public String toString() {
        return "IOBuffer, commited: " + fwlength + ", buffer: " + wlength;
    }

    public void saveToFile(String file) throws IOException {
        saveToFile(new File(file));
    }

    public void saveToFile(File file) throws IOException {
        if (file == null)
            return;
        try (BufferedOutputStream fout = new BufferedOutputStream(
                new FileOutputStream(file), 100 * 1024);) {
            IOUtils.copy(getInputStream(), fout);
        }
    }

    private byte[] bmd5;
    private boolean md5called = false;

    public byte[] getHashB() throws IOException {
        if (bmd5 == null && md5called)
            throw new IOException("Wielokrotne wywołanie generatora sumy kontrolnej");

        if (bmd5 == null) {
            bmd5 = digest.digest();
            md5called = true;
        }

        return bmd5;
    }

    public String getHashS() throws IOException {
        return digest != null ? Hex.toString(getHashB()) : null;
    }

    private void initialize() throws IOException {
        initialized = true;
        try {
            if (hash != null)
                digest = MessageDigest.getInstance(hash.name());
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }
    }

    public long length() {
        return wlength;
    }

    public static interface IWriteCallback {

        public void onWrite(int b) throws IOException;

        public void onFlush(byte[] b) throws IOException;

        public void onClose() throws IOException;

    }

    private boolean initialized = false;

    @Override
    public void write(int b) throws IOException {
        if (!initialized)
            initialize();

        bmd5 = null; // musi byc resetowane

        if (writeCallback != null)
            writeCallback.onWrite(b);

        if (digest != null)
            digest.update((byte) (b & 0xFF));

        if (wpos >= wbuffer.length)
            flush();
        wbuffer[wpos++] = (byte) b;
        wlength++;
        isModified = true;
        if (autoFlushThreshold > 0 && autoFlushThreshold % wlength == 0)
            flush();
    }

    @Override
    public void flush() throws IOException {

        if (deleted)
            throw new IOException(CDataRemoved);

        synchronized (sync) {
            if (wlength > memoryBufferSizeLimit && file == null) {
                file = File.createTempFile("IOBUFF_", null);
                try (FileOutputStream fout = new FileOutputStream(file)) {
                    fout.write(bout.toByteArray());
                    bout = new ByteArrayOutputStream();
                }
            }

            int size = (int) (wpos);
            fwlength += size;

            byte[] copy = Arrays.copyOf(wbuffer, size);

            if (writeCallback != null)
                writeCallback.onFlush(copy);

            if (file != null) {
                try (FileOutputStream fout = new FileOutputStream(file, true)) {
                    fout.write(copy);
                }
            } else
                bout.write(copy);

            wpos = 0;
            isModified = false;
        }
    }

    @Override
    public void close() throws IOException {
        if (writeCallback != null)
            writeCallback.onClose();

        if (deleteOnClose)
            delete();

    }

    public void delete() {
        try {
            if (file != null)
                file.delete();
        } finally {
            bout = null;
            file = null;
            deleted = true;
        }
    }

    public class IOBufferInput extends InputStream {

        private byte[] rbuffer = new byte[0];
        private long rlenght = 0;
        int rpos = 0;

        public long getRededLength() {
            return rlenght;
        }

        @Override
        public int read() throws IOException {

            if (autoFlush && fwlength < wlength)
                flush();

            if (rlenght >= fwlength)
                return -1;

            if (rpos >= rbuffer.length)
                readBlock();

            ++rlenght;
            return rbuffer[rpos++] & 0xFF;
        }

        private IOBufferInput() {
        }

        @Override
        public int available() {
            return (int) (fwlength - rlenght);
        }

        @Override
        public void close() {
            rbuffer = new byte[0];
        }

        private void readBlock() throws IOException {
            if (deleted)
                throw new IOException(CDataRemoved);
            synchronized (sync) {

                if (isModified)
                    Log.warning("IOBuffer", "Dane zostały zmodyfikowane");

                long size = fwlength - rlenght;
                if (size > 1024 * 100)
                    size = 1024 * 100;

                rbuffer = new byte[(int) size];
                rpos = 0;

                if (file != null) {
                    try (RandomAccessFile raf = new RandomAccessFile(file, "r");) {
                        raf.seek(rlenght);
                        raf.read(rbuffer);
                    }
                    return;
                }
                byte[] arr = bout.toByteArray();

                for (int i = 0; i < size; i++)
                    rbuffer[i] = arr[(int) rlenght + i];
            }
        }
    }

    public IOBufferInput getInputStream() {
        return new IOBufferInput();
    }

    public boolean isModified() {
        return isModified;
    }

    public byte[] getData() throws IOException {
        //   if (autoFlush)            flush();
        return IOUtils.copy(getInputStream());
    }

}
