package com.json.exceptions;

import com.json.JCollection;

public class JNotFound extends JException {

    public JNotFound(JCollection parent, String... names) {

    }

    public JNotFound(JCollection parent, int index) {
        super("Nie znaleziono elementu " + index);
    }
}
