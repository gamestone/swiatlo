package com.io;

import com.Utils;
import mlogger.Log;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**

 @author milosz
 */
public class IOUtils {
    
    private final static int defBuffSize = 102400;
    //------------------------------------------------

    public static byte[] copy(File file) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(file), defBuffSize);) {
            return copy(in, false, -1, defBuffSize);
        }
    }
    
    public static byte[] copy(InputStream in) throws IOException {
        return copy(in, false, -1, defBuffSize);
    }
    
    public static byte[] copy(InputStream in, boolean closeStreams,
            long maxLength, int buffSize) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        copy(in, bout, closeStreams, maxLength, buffSize);
        return bout.toByteArray();
    }
    
    public static long copy(File file, OutputStream out) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(file), defBuffSize);) {
            return copy(in, out, -1, false);
        }
    }
    
    public static long copy(InputStream in, File outFile) throws IOException {
        createFileDir(outFile);
        try (BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(outFile), defBuffSize);) {
            return copy(in, out, -1, false);
        }
    }
    
    private static void createFileDir(File file) {
        if (file == null)
            return;
        File parent = file.getParentFile();
        if (parent != null
                && !parent.getPath().equals("")
                && !parent.getPath().equals("/"))
            parent.mkdirs();
    }
    
    public static long copy(File inFile, File outFile) throws IOException {
        createFileDir(outFile);
        try (BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(inFile), defBuffSize);
             BufferedOutputStream out = new BufferedOutputStream(
                     new FileOutputStream(outFile), defBuffSize);) {
            return copy(in, out, -1, false);
        }
    }
    
    public static long copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, false, -1, defBuffSize);
    }
    
    public static long copy(InputStream in, OutputStream out,
            boolean closeStreams) throws IOException {
        return copy(in, out, closeStreams, -1, defBuffSize);
    }
    
    public static long copy(InputStream in, OutputStream out, long maxLength,
            boolean closeStreams) throws IOException {
        return copy(in, out, closeStreams, maxLength, defBuffSize);
    }
    
    public static long copy(InputStream in, OutputStream out,
            boolean closeStreams, long maxLength, int buffSize) throws IOException {
        long total = 0;
        byte[] buff = new byte[maxLength > 0 && maxLength < buffSize
                               ? (int) maxLength : buffSize];
        try {
            while (true) {
                int max = buff.length;
                if (maxLength > 0 && total + max > maxLength)
                    max = (int) (maxLength - total);
                int len = in.read(buff, 0, max);
                if (len <= 0)
                    break;
                out.write(buff, 0, len);
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
            return copy(in, dOut);
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
            return copy(dIn, out);
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
    
    public static void append(String fileName, String text) throws IOException {
        if (text != null && fileName != null)
            append(fileName, text.getBytes(Charset.forName("UTF-8")));
    }
    
    public static void append(String file, byte[] data) throws IOException {
        append(new File(file), data);
    }
    
    public static void append(File file, byte[] data) throws IOException {
        if (file != null && data != null)
            try (FileOutputStream fos = new FileOutputStream(file, true);) {
                fos.write(data);
            }
    }
    
    public static String readUtf(InputStream in) throws IOException {
        return new String(copy(in), Charset.forName("UTF-8"));
    }
    
    public static String read(InputStream in, Charset charset) throws IOException {
        return new String(copy(in), charset);
    }
    
}
