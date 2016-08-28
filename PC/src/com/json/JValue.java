package com.json;

import java.io.IOException;

/**
 * Miłosz Ziernik 2014/04/15
 */
public class JValue extends JElement {

    private final Object value;

    public JValue(final Object value) {
        super();
        this.value = value;
    }

    public Object value() {
        return value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isString() {
        return value instanceof String;
    }

    public String asString() {
        if (value == null)
            return null;
        return value.toString();
    }

    public boolean isNumber() {
        return value instanceof Number;
    }

    public Number asNumber() {
        return (Number) value;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    public Boolean asBoolean() {
        return (Boolean) value;
    }

    @Override
    public boolean remove() {
        return parent != null ? parent.doRemove(this) : false;
    }

    @Override
    public void move(JCollection destination) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveChildren(JCollection destination) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void print(Appendable writer, String intent) throws IOException {

        if (value == null) {
            writer.append("null");
            return;
        }

        JOptions opt = getOptions();

        writer.append(intent);

        if (isBoolean() || isNumber())
            writer.append(value.toString());
        else {
            writer.append("\"");
            escape(value.toString(), writer, opt.escapeUnicode());
            writer.append("\"");
        }
    }
}
