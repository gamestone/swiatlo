package com.logs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * Miłosz Ziernik 2013/01/22
 */
public class PostData {

    public final HttpURLConnection conn;
    public int connectTimeout = 3000;
    public int readTimeout = 20000;

    public PostData(String url) throws IOException {
        conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
    }

    public PostData(String url, Proxy proxy) throws IOException {
        conn = (HttpURLConnection) new URL(url).openConnection(
                proxy != null ? proxy : Proxy.NO_PROXY);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
    }

    public void writeRequest(InputStream inputStream, boolean closeStream)
            throws UnsupportedEncodingException, IOException {
        if (inputStream == null)
            return;
        try {
            conn.setDoOutput(true);
            byte[] buff = new byte[10240];
            int len;
            OutputStream out = conn.getOutputStream();
            try {
                while ((len = inputStream.read(buff)) > 0)
                    out.write(buff, 0, len);
                out.flush();
            } finally {
                out.close();
            }
        } finally {
            if (closeStream)
                inputStream.close();
        }
    }

    public void writeRequest(Reader reader, String encoding,
            boolean closeStream) throws UnsupportedEncodingException, IOException {
        if (reader == null)
            return;
        try {
            conn.setDoOutput(true);
            BufferedWriter wr = new BufferedWriter(
                    new OutputStreamWriter(
                            conn.getOutputStream(), encoding));
            try {
                int charsRead;
                char[] cbuf = new char[1024];
                while ((charsRead = reader.read(cbuf)) != -1)
                    wr.write(cbuf, 0, charsRead);
                wr.flush();
            } finally {
                wr.close();
            }
        } finally {
            if (closeStream)
                reader.close();
        }
    }

    public void readResponse(OutputStream outputStream, boolean closeStream)
            throws IOException {
        if (outputStream == null)
            return;
        try {
            InputStream in;
            try {
                in = conn.getInputStream();
            } catch (Exception e) {
                in = conn.getErrorStream();
            }

            if (in == null)
                return;

            byte[] buff = new byte[10240];
            int len;
            try {
                while ((len = in.read(buff)) > 0)
                    outputStream.write(buff, 0, len);
                outputStream.flush();
            } finally {
                in.close();
            }
            outputStream.flush();
        } finally {
            if (closeStream)
                outputStream.close();
        }
    }

    public void readResponse(Writer writer, String encoding,
            boolean closeStream) throws IOException {
        if (writer == null)
            return;
        try {
            InputStream in;
            try {
                in = conn.getInputStream();
            } catch (Exception e) {
                in = conn.getErrorStream();
            }

            if (in == null)
                return;

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(in, encoding));
            try {
                String s;
                while ((s = rd.readLine()) != null)
                    writer.write(s);
            } finally {
                rd.close();
            }

            writer.flush();
        } finally {
            if (closeStream)
                writer.close();
        }
    }

    public String readResponse(String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        readResponse(sw, encoding, true);
        return sw.toString();
    }
}
