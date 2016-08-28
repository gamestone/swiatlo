package com.xml;

import com.Exceptions;
import java.io.IOException;
import org.xml.sax.SAXParseException;

/**
 * Miłosz Ziernik
 * 2013/11/20 
 */
public class XmlException extends IOException {

    public XmlException(String message) {
        super(message);
    }

    public XmlException(Exception e) {
        super("Błąd parsowania XML, " + Exceptions.exceptionToStr(e)
                + (e instanceof SAXParseException
                   ? "; linia: " + ((SAXParseException) e).getLineNumber()
                + ", kulumna: " + ((SAXParseException) e).getColumnNumber()
                   : ""));
    }
}
