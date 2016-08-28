package com.utils;

import com.*;
import mlogger.Log;
import java.text.ParseException;

/**
 * Miłosz Ziernik
 * 2013/11/23 
 */
public class VParser {

    public static class NullValueException extends IllegalArgumentException {

        public NullValueException(String valueName, String message) {
            super(String.format(message, valueName));
        }
    }

    public static class EmptyValueException extends IllegalArgumentException {

        public EmptyValueException(String valueName, String message) {
            super(String.format(message, valueName));
        }
    }

    public static class IncorrectValueException extends IllegalArgumentException {

        public IncorrectValueException(String valueName, String message, Exception ex) {
            super(String.format(message, valueName), ex);
        }
    }

    public String eNullValue = "Wartość \"%s\" nie może być pusta";
    public String eEmptyValue = "Wartość \"%s\" nie może być pusta";
    public String eIncorrectValue = "Nieprawidłowa wartość \"%s\"";

    public final String value;
    public final String valueName;
    public boolean logExceptions = false;

    public VParser(String value, String valueName) {
        this.value = value;
        this.valueName = valueName;
    }

    public String getStr(boolean canBeEmpty) {
        if (!canBeEmpty && value == null)
            throw new NullValueException(valueName, eNullValue);

        if (!canBeEmpty && value.toString().trim().isEmpty())
            throw new EmptyValueException(valueName, eEmptyValue);

        return value != null ? value.toString() : value;
    }
    
    public Boolean getBool(Boolean def) {
        String val = getStr(true);
        try {
            Boolean res = Utils.strBool(val, def);
            if (res == null)
                return def;
            return res;
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public boolean getBool() {
        String val = getStr(false);
        try {
            Boolean res = Utils.strBool(val, null);
            if (res == null)
                throw new IncorrectValueException(valueName, eIncorrectValue, null);
            return res;
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }

    public Byte getByte(Byte def) {
        try {
            return Byte.parseByte(getStr(true));
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public byte getByte() {
        String val = getStr(false);
        try {
            return Byte.parseByte(val);
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }

    public Short getShort(Short def) {
        try {
            return Short.parseShort(getStr(true));
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public short getShort() {
        String val = getStr(false);
        try {
            return Short.parseShort(val);
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }

    public Integer getInt(Integer def) {
        try {
            return Integer.parseInt(getStr(true));
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public int getInt() {
        String val = getStr(false);
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }

    public Long getLong(Long def) {
        try {
            return Long.parseLong(getStr(true));
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public long getLong() {
        String val = getStr(false);
        try {
            return Long.parseLong(val);
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }

    public Float getFloat(Float def) {
        try {
            return Float.parseFloat(getStr(true));
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public float getFloat() {
        String val = getStr(false);
        try {
            return Float.parseFloat(val);
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }

    public Double getDouble(Double def) {
        try {
            return Double.parseDouble(getStr(true));
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public double getDouble() {
        String val = getStr(false);
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }

    public TDate getDate(TDate def) {
        try {
            return new TDate(getStr(true));
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            return def;
        }
    }

    public TDate getDate() {
        String val = getStr(false);
        try {
            return new TDate(val);
        } catch (Exception e) {
            if (logExceptions)
                Log.warning("Parser", e);
            throw new IncorrectValueException(valueName, eIncorrectValue, e);
        }
    }
}
