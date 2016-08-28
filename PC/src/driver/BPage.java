package driver;

import com.sun.net.httpserver.*;
import com.utils.Params;
import java.net.URI;

/**
 * Miłosz Ziernik
 * 2014/07/13 
 */
public abstract class BPage {

    protected HttpExchange http;
    protected Headers requestHeaders;
    protected Headers responseHeaders;
    protected Params params;

    protected abstract void processRequest();

    public BPage init(HttpExchange he) {
        this.http = he;
        requestHeaders = he.getRequestHeaders();
        responseHeaders = he.getResponseHeaders();
        responseHeaders.set("Content-Type", "text/html; charset=UTF-8");

        URI uri = he.getRequestURI();
        params = Params.fromURI(uri.getQuery());
        
        return this;
    }

}
