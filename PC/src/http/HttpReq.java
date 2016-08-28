package http;

import com.io.IOUtils;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;

public class HttpReq extends HttpExchange {

    private final HttpExchange http;
    public final String[] path;

    private boolean isCommited;

    public boolean isCommited() {
        return isCommited;
    }

    public HttpReq(final HttpExchange http) {
        this.http = http;

        String p = http.getRequestURI().getPath();
        if (p.startsWith("/"))
            p = p.substring(1);

        if (p.endsWith("/"))
            p = p.substring(0, p.length() - 1);
        path = p.split("\\/");
    }

    @Override
    public Headers getRequestHeaders() {
        return http.getRequestHeaders();
    }

    @Override
    public Headers getResponseHeaders() {
        return http.getResponseHeaders();
    }

    @Override
    public URI getRequestURI() {
        return http.getRequestURI();
    }

    @Override
    public String getRequestMethod() {
        return http.getRequestMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return http.getHttpContext();
    }

    @Override
    public void close() {
        http.close();
    }

    @Override
    public InputStream getRequestBody() {
        return http.getRequestBody();
    }

    @Override
    public OutputStream getResponseBody() {
        isCommited = true;
        return http.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int i, long l) throws IOException {
        http.sendResponseHeaders(i, l);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return http.getRemoteAddress();
    }

    @Override
    public int getResponseCode() {
        return http.getResponseCode();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return http.getLocalAddress();
    }

    @Override
    public String getProtocol() {
        return http.getProtocol();
    }

    @Override
    public Object getAttribute(String string) {
        return http.getAttribute(string);
    }

    @Override
    public void setAttribute(String string, Object o) {
        http.setAttribute(string, o);
    }

    @Override
    public void setStreams(InputStream in, OutputStream out) {
        isCommited = true;
        http.setStreams(in, out);
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return http.getPrincipal();
    }

    public void returnResource(String file) throws IOException {
        Headers resp = getResponseHeaders();

        String ext = file.contains(".")
                ? file.substring(file.lastIndexOf(".") + 1).toLowerCase() : "";

        for (String[] ss : MimeMappings.mappings)
            if (ss[0].equals(ext)) {
                resp.set("Content-Type", ss[1]);
                break;
            }

        File f = new File("X:\\Projekty\\Sterownik\\SterownikPI\\src\\", file);

        try (InputStream in = f.exists()
                ? new BufferedInputStream(new FileInputStream(f))
                : getClass().getResourceAsStream(file)) {
            {
                sendResponseHeaders(200, in.available());

                try (OutputStream out = getResponseBody()) {

                    IOUtils.copy(in, out);
                }
            }

        }

    }
}
