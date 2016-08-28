package com.json;

/**
 * Miłosz Ziernik
 * 2014/06/10 
 */
public class JOptions {

    private Boolean escapeUnicode;
    private String intent;
    private Boolean acceptNulls;
    private Boolean useQuota;
    private String lineBreakChar;
    private Boolean singleLine;
    private final JCollection element;

    public JOptions(JCollection element) {
        this.element = element;
    }

    public boolean escapeUnicode() {
        JCollection el = element;
        while (el != null) {
            if (el.options.escapeUnicode != null)
                return el.options.escapeUnicode;
            el = el.parent;
        }
        return false;
    }

    public JOptions escapeUnicode(Boolean escapeUnicode) {
        this.escapeUnicode = escapeUnicode;
        return this;
    }

    public boolean acceptNulls() {
        JCollection el = element;
        while (el != null) {
            if (el.options.acceptNulls != null)
                return el.options.acceptNulls;
            el = el.parent;
        }
        return true;
    }

    public JOptions acceptNulls(Boolean acceptNulls) {
        this.acceptNulls = acceptNulls;
        return this;
    }

    public boolean quotaNames() {
        JCollection el = element;
        while (el != null) {
            if (el.options.useQuota != null)
                return el.options.useQuota;
            el = el.parent;
        }
        return true;
    }

    public JOptions quotaNames(Boolean useQuota) {
        this.useQuota = useQuota;
        return this;
    }

    public String intent() {
        JCollection el = element;
        while (el != null) {
            if (el.options.intent != null)
                return el.options.intent;
            el = el.parent;
        }
        return "  ";
    }

    public JOptions intent(String intent) {
        this.intent = intent;
        return this;
    }

    public String lineBreakChar() {
        JCollection el = element;
        while (el != null) {
            if (el.options.lineBreakChar != null)
                return el.options.lineBreakChar;
            el = el.parent;
        }
        return "\n";
    }

    public JOptions lineBreakChar(String lineBreakChar) {
        this.lineBreakChar = lineBreakChar;
        return this;
    }

    public JOptions singleLine(Boolean singleLine) {
        this.singleLine = singleLine;
        return this;
    }

    public Boolean singleLine() {
        JCollection el = element;
        while (el != null) {
            if (el.options.singleLine != null)
                return el.options.singleLine;
            el = el.parent;
        }
        return null;
    }

}
