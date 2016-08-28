package com.logs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FieldWatcher implements Runnable {

    private final Thread thread = new Thread(this);
    public final List<FField> fields = new LinkedList<FField>(); // synchronizowane
    private final IFieldChange onChange;

    public FieldWatcher(final IFieldChange onChange) {
        this.onChange = onChange;
    }

    public static interface IFieldChange {

        public void onFieldChange(FField field);
    }

    public class FField {

        public final Field field;
        public final Object object;
        public String value;
        public boolean readed = false;

        public FField(final Field field, final Object object) {
            this.field = field;
            this.object = object;
        }
    }

    public void removeField(Object object, String fieldName) {
        if (object == null || fieldName == null)
            return;
        try {
            Field field = object.getClass().getField(fieldName);
            synchronized (fields) {
                for (FField ff : fields)
                    if (ff.field == field && ff.object == object) {
                        fields.remove(ff);
                        return;
                    }
            }
        } catch (Exception e) {
        }
    }

    public void addField(Object object, String fieldName) {
        if (object == null || fieldName == null)
            return;
        try {
            Field field = object.getClass().getField(fieldName);
            synchronized (fields) {
                for (FField ff : fields)
                    if (ff.field == field && ff.object == object)
                        return;

                fields.add(new FField(field, object));
            }
            if (thread.getState() == Thread.State.NEW)
                thread.start();
        } catch (Exception e) {
        }
    }

    public void addFields(Object object, boolean includeFinal) {
        if (object == null)
            return;
        try {
            for (Field field : object.getClass().getFields()) {
                int mods = field.getModifiers();

                if (!includeFinal && Modifier.isFinal(mods))
                    continue;

                addField(object, field.getName());
            }

        } catch (Exception e) {
        }
    }

    @Override
    public void run() {
        while (true)
            try {
                synchronized (fields) {
                    for (FField ff : fields)
                        try {
                            String str = getFieldValue(ff.field, ff.object);
                            boolean diff = !ff.readed
                                    || (str != null && !str.equals(ff.value))
                                    || (ff.value != null && !ff.value.equals(str));
                            ff.readed = true;
                            ff.value = str;
                            if (diff && onChange != null)
                                onChange.onFieldChange(ff);

                        } catch (Exception ex) {
                        }
                }

                Thread.sleep(100);
            } catch (InterruptedException ex) {
                return;
            }
    }

    private static String getFieldValue(Field field, Object obj)
            throws IllegalArgumentException, IllegalAccessException {
        Class<?> type = field.getType();
        if (type == Boolean.TYPE)
            return Boolean.toString(field.getBoolean(obj));
        else
            if (type == Byte.TYPE)
                return Byte.toString(field.getByte(obj));
            else
                if (type == Short.TYPE)
                    return Short.toString(field.getShort(obj));
                else
                    if (type == Integer.TYPE)
                        return Integer.toString(field.getInt(obj));
                    else
                        if (type == Long.TYPE)
                            return Long.toString(field.getLong(obj));
                        else
                            if (type == Float.TYPE)
                                return Float.toString(field.getFloat(obj));
                            else
                                if (type == Double.TYPE)
                                    return Double.toString(field.getDouble(obj));
                                else
                                    if (type == Character.TYPE)
                                        return Character.toString(field.getChar(obj));

        Object val = field.get(obj);

        if (type == boolean[].class)
            return Arrays.toString((boolean[]) val);
        else
            if (type == Boolean[].class)
                return Arrays.toString((Boolean[]) val);
            else
                if (type == byte[].class)
                    return Arrays.toString((byte[]) val);
                else
                    if (type == Byte[].class)
                        return Arrays.toString((Byte[]) val);
                    else
                        if (type == short[].class)
                            return Arrays.toString((short[]) val);
                        else
                            if (type == Short[].class)
                                return Arrays.toString((Short[]) val);
                            else
                                if (type == int[].class)
                                    return Arrays.toString((int[]) val);
                                else
                                    if (type == Integer[].class)
                                        return Arrays.toString((Integer[]) val);
                                    else
                                        if (type == long[].class)
                                            return Arrays.toString((long[]) val);
                                        else
                                            if (type == Long[].class)
                                                return Arrays.toString((Long[]) val);
                                            else
                                                if (type == float[].class)
                                                    return Arrays.toString((float[]) val);
                                                else
                                                    if (type == Float[].class)
                                                        return Arrays.toString((Float[]) val);
                                                    else
                                                        if (type == double[].class)
                                                            return Arrays.toString((double[]) val);
                                                        else
                                                            if (type == Double[].class)
                                                                return Arrays.toString((Double[]) val);
                                                            else
                                                                if (type == char[].class)
                                                                    return Arrays.toString((char[]) val);
                                                                else
                                                                    if (type == Character[].class)
                                                                        return Arrays.toString((Character[]) val);
                                                                    else
                                                                        if (type == String[].class)
                                                                            return Arrays.toString((String[]) val);
                                                                        else
                                                                            return val != null ? val.toString() : null;
    }
}
