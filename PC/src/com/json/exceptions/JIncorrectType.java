package com.json.exceptions;

import com.json.JElement;

public class JIncorrectType extends JException {

    public final JElement source;
    public final JElement destination;

    public JIncorrectType(JElement source, JElement destination) {
        super("Nieprawidłowy typ obiektu (oczekiwany: " + source + ", aktualny: " + destination);
        this.source = source;
        this.destination = destination;
    }

}
