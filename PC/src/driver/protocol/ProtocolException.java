package driver.protocol;

import java.io.IOException;

/**
 * Miłosz Ziernik
 * 2014/07/13 
 */
public class ProtocolException extends IOException {

    public ProtocolException() {
        super();
    }

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }
}
