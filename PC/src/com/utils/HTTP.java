package com.utils;


import java.io.*;
import java.net.*;
import java.util.*;

public class HTTP {

    public static class HttpResponse {

        public String contentType;
        public String contentEncoding;

        public HttpResponse(HttpURLConnection conn) {
        }

    }

    public static byte[] httpGet(final String sUrl) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        download(sUrl, bout);
        return bout.toByteArray();
    }

    public static HttpURLConnection download(String sUrl, OutputStream out) throws IOException {
        URL url = new URL(sUrl);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        //   CProxy.setAuthorization(conn);
        conn.connect();
        try {
            try (InputStream in = conn.getInputStream()) {

                if (out == null)
                    return conn;

                byte[] buf = new byte[102400];
                int len;
                while ((len = in.read(buf)) >= 0)
                    out.write(buf, 0, len);

            } finally {
                out.flush();
                out.close();
            }

            conn.getResponseCode();

        } finally {
            conn.disconnect();
        }
        return conn;
    }

    public static byte[] multipartRequest(URL url, Map<String, String> params,
            String fieldName, String fileName, byte[] buff, String contentType)
            throws IOException {

        String boundary = "----------gPQhM7fvh9IuBxlofDVzXjZLVbIJa91bb7bk8tyk";//----------V2ymHFg03ehbqgZCaKO6jy
        String endBoundary = "\r\n--" + boundary + "--\r\n";

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        conn.setDoOutput(true);
        conn.setDoInput(true);

        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");

        if (params != null)
            for (String key : params.keySet()) {
                sb.append("Content-Disposition: form-data; name=\"").append(key);
                sb.append("\"\r\n").append("\r\n").append(params.get(key)).
                        append("\r\n");
                sb.append("--").append(boundary).append("\r\n");
            }

        sb.append("Content-Disposition: form-data; name=\"").append(fieldName);
        sb.append("\"; filename=\"").append(fileName).append("\"\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n\r\n");

        try (OutputStream dOut = conn.getOutputStream()) {
            dOut.write(sb.toString().getBytes());
            dOut.write(buff);
            dOut.write(endBoundary.getBytes());
        }

        byte[] buf = new byte[10240];
        int len;
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            while ((len = is.read(buf)) > 0)
                bOut.write(buf, 0, len);

            return bOut.toByteArray();
        } finally {
            is.close();
            bOut.close();
        }
    }

}
