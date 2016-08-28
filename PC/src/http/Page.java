package http;

import com.html.core.HtmlBuilder;
import com.sun.net.httpserver.Headers;
import java.io.OutputStream;

public abstract class Page {

    public final HttpReq http;
    public final HtmlBuilder html = new HtmlBuilder();
    public final Headers response;
    public final Headers request;

    public Page(HttpReq http) {
        this.http = http;
        this.request = http.getRequestHeaders();
        this.response = http.getResponseHeaders();
    }

    void doProcessRequest() throws Exception {
        processRequest();

        if (!http.isCommited()) {
            response.set("Content-Type", "text/html; charset=UTF-8");
            http.sendResponseHeaders(200, 0);
            try (OutputStream out = http.getResponseBody()) {
                html.returnHTML(out);
            }
        }
    }

    public abstract void processRequest() throws Exception;

}
