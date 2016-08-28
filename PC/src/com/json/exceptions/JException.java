package com.json.exceptions;

import java.io.IOException;

/**
 * Miłosz Ziernik
 * 2014/06/10 
 */
public class JException extends IOException {

    public JException() {
        super();
    }

    public JException(String message) {
        super(message);
    }

    public JException(String message, Throwable cause) {
        super(message, cause);
    }

    public JException(Throwable cause) {
        super(cause);
    }

}
