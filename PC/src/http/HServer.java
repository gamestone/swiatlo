package http;

import com.html.core.HtmlBuilder;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import mlogger.Log;
import mlogger.utils.Exceptions;

public class HServer {

    public static void init() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        HttpContext ctx = server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor

        server.start();
    }

    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            long ts = System.currentTimeMillis();
            final HttpReq req = new HttpReq(t);
            String path = t.getRequestURI().getPath();

            Headers hdr = req.getResponseHeaders();

            hdr.set("Pragma", "no-cache");
            hdr.set("Cache-Control", "no-cache, no-store, must-revalidate");
            hdr.set("Expires", "0");

            try {

                if (path.equals("/"))
                    path = "/swiatlo.html";

                if (path.equals("/favicon.ico")) {
                    req.returnResource("/res/favicon.ico");
                    return;
                }

                if (getClass().getResource("/res" + path) != null) {
                    req.returnResource("/res" + path);
                    return;
                }

                throw new FileNotFoundException(t.getRequestURI().getPath());
            } catch (Throwable e) {
                Log.error(e);
                if (req.isCommited())
                    return;

                int httpStatus = 500;

                if (e instanceof FileNotFoundException)
                    httpStatus = 404;

                Headers resp = req.getResponseHeaders();
                resp.set("Content-Type", "text/plain");

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));

                byte[] data = sw.toString().getBytes("UTF-8");
                t.sendResponseHeaders(httpStatus, data.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(data);
                }

            } finally {
                Log.event("req", t.getRequestURI() + " " + (System.currentTimeMillis() - ts) + "ms");
            }
            //     
        }

    }

    private void errorPage(Throwable e, HttpExchange he) {
        Log.error(e);
        try {
            HtmlBuilder html = new HtmlBuilder();
            html.body.h1().text(Exceptions.toString(e));

            //   he.getResponseHeaders();
            html.ul().addIems(Exceptions.getStackTraceStr(e).getList());

            he.sendResponseHeaders(500, 0);

            try (OutputStream out = he.getResponseBody();) {
                html.returnHTML(out);
            }

        } catch (Throwable exc) {
            Log.error(exc);
        }
    }
}
