package com;

import com.utils.*;
import mlogger.Log;
import java.lang.reflect.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;

public class FieldUtils {

    public static boolean isSimpleField(Field field, Boolean inclArrays) {
        if (field == null)
            return false;
        return isSimpleField(field.getType(), inclArrays);

    }

    public static Object strToObject(Type type, String val) {
        if (type == null || val == null)
            return null;

        if (type == Boolean.TYPE || type == Boolean.class)
            return Boolean.valueOf(val);

        if (type == Byte.TYPE || type == Byte.class)
            return Byte.valueOf(val);

        if (type == Short.TYPE || type == Short.class)
            return Short.valueOf(val);

        if (type == Integer.TYPE || type == Integer.class)
            return Integer.valueOf(val);

        if (type == Long.TYPE || type == Long.class)
            return Long.valueOf(val);

        if (type == Float.TYPE || type == Float.class)
            return Float.valueOf(val);

        if (type == Double.TYPE || type == Double.class)
            return Double.valueOf(val);

        if (type == Character.TYPE || type == Character.class)
            return val.length() > 0 ? val.charAt(0) : null;

        if (type instanceof Class && Utils.classExtends((Class) type, Enum.class)) {
            for (Field field : ((Class) type).getFields())
                if (field.getName().equals(val))
                    try {
                        return field.get(null);
                    } catch (Exception ex) {
                        throw new UnsupportedOperationException(ex);
                    }
            throw new UnsupportedOperationException("Nieprawidłowa wartość  " + val);
        }

        if (type == String.class)
            return val;

        throw new UnsupportedOperationException("Nieobsługiwany typ " + type.toString());
    }

    public static boolean isSimpleField(Class<?> type, Boolean inclArrays) {
        if (type == null)
            return false;

        return type == Boolean.TYPE || type == Boolean.class
                || type == Byte.TYPE || type == Byte.class
                || type == Short.TYPE || type == Short.class
                || type == Integer.TYPE || type == Integer.class
                || type == Long.TYPE || type == Long.class
                || type == Float.TYPE || type == Float.class
                || type == Double.TYPE || type == Double.class
                || type == Character.TYPE || type == Character.class
                || type == String.class
                || (inclArrays && (type == boolean[].class
                || type == Boolean[].class
                || type == byte[].class || type == Byte[].class
                || type == short[].class || type == Short[].class
                || type == int[].class || type == Integer[].class
                || type == long[].class || type == Long[].class
                || type == float[].class || type == Float[].class
                || type == double[].class || type == Double[].class
                || type == char[].class || type == Character[].class
                || type == String[].class));
    }

    public static String[] getFieldValues(Field field, Object obj)
            throws IllegalArgumentException, IllegalAccessException, ParseException {
        Class<?> type = field.getType();
        Object val = field.get(obj);
        List<String> lst = new LinkedList<>();

        if (val == null) {
        } else
            if (type == boolean[].class) {
                boolean[] arr = (boolean[]) val;
                for (boolean v : arr)
                    lst.add(Boolean.toString(v));
            } else
                if (type == Boolean[].class) {
                    Boolean[] arr = (Boolean[]) val;
                    for (Boolean v : arr)
                        lst.add(Boolean.toString(v));
                } else
                    if (type == byte[].class) {
                        byte[] arr = (byte[]) val;
                        for (byte v : arr)
                            lst.add(Byte.toString(v));
                    } else
                        if (type == Byte[].class) {
                            Byte[] arr = (Byte[]) val;
                            for (Byte v : arr)
                                lst.add(Byte.toString(v));
                        } else
                            if (type == short[].class) {
                                short[] arr = (short[]) val;
                                for (short v : arr)
                                    lst.add(Short.toString(v));
                            } else
                                if (type == Short[].class) {
                                    Short[] arr = (Short[]) val;
                                    for (Short v : arr)
                                        lst.add(Short.toString(v));
                                } else
                                    if (type == int[].class) {
                                        int[] arr = (int[]) val;
                                        for (int v : arr)
                                            lst.add(Integer.toString(v));
                                    } else
                                        if (type == Integer[].class) {
                                            Integer[] arr = (Integer[]) val;
                                            for (Integer v : arr)
                                                lst.add(Integer.toString(v));
                                        } else
                                            if (type == float[].class) {
                                                float[] arr = (float[]) val;
                                                for (float v : arr)
                                                    lst.add(Float.toString(v));
                                            } else
                                                if (type == Float[].class) {
                                                    Float[] arr = (Float[]) val;
                                                    for (Float v : arr)
                                                        lst.add(Float.toString(v));
                                                } else
                                                    if (type == double[].class) {
                                                        double[] arr = (double[]) val;
                                                        for (double v : arr)
                                                            lst.add(Double.toString(v));
                                                    } else
                                                        if (type == Double[].class) {
                                                            Double[] arr = (Double[]) val;
                                                            for (Double v : arr)
                                                                lst.add(Double.toString(v));
                                                        } else
                                                            if (type == char[].class) {
                                                                char[] arr = (char[]) val;
                                                                for (char v : arr)
                                                                    lst.add(Character.toString(v));
                                                            } else
                                                                if (type == Character[].class) {
                                                                    Character[] arr = (Character[]) val;
                                                                    for (Character v : arr)
                                                                        lst.add(Character.toString(v));
                                                                } else
                                                                    if (type == String[].class) {
                                                                        String[] arr = (String[]) val;
                                                                        for (String v : arr)
                                                                            lst.add(v);
                                                                    } else
                                                                        throw new ParseException("Nieobsługiwany typ danych: " + type.getName(), 0);

        String[] res = new String[lst.size()];
        lst.toArray(res);
        return res;
    }

    public static String getFieldValue(Field field, Object obj)
            throws IllegalArgumentException, IllegalAccessException {
        Class<?> type = field.getType();
        if (type == Boolean.TYPE)
            return Boolean.toString(field.getBoolean(obj));

        if (type == Byte.TYPE)
            return Byte.toString(field.getByte(obj));

        if (type == Short.TYPE)
            return Short.toString(field.getShort(obj));

        if (type == Integer.TYPE)
            return Integer.toString(field.getInt(obj));

        if (type == Long.TYPE)
            return Long.toString(field.getLong(obj));

        if (type == Float.TYPE)
            return Float.toString(field.getFloat(obj));

        if (type == Double.TYPE)
            return Double.toString(field.getDouble(obj));

        if (type == Character.TYPE)
            return Character.toString(field.getChar(obj));

        Object val = field.get(obj);

        if (type == boolean[].class)
            return Arrays.toString((boolean[]) val);
        else
            if (type == Boolean[].class)
                return Arrays.toString((Boolean[]) val);

        if (type == byte[].class)
            return Arrays.toString((byte[]) val);

        if (type == Byte[].class)
            return Arrays.toString((Byte[]) val);

        if (type == short[].class)
            return Arrays.toString((short[]) val);

        if (type == Short[].class)
            return Arrays.toString((Short[]) val);

        if (type == int[].class)
            return Arrays.toString((int[]) val);

        if (type == Integer[].class)
            return Arrays.toString((Integer[]) val);

        if (type == long[].class)
            return Arrays.toString((long[]) val);

        if (type == Long[].class)
            return Arrays.toString((Long[]) val);

        if (type == float[].class)
            return Arrays.toString((float[]) val);

        if (type == Float[].class)
            return Arrays.toString((Float[]) val);

        if (type == double[].class)
            return Arrays.toString((double[]) val);

        if (type == Double[].class)
            return Arrays.toString((Double[]) val);

        if (type == char[].class)
            return Arrays.toString((char[]) val);

        if (type == Character[].class)
            return Arrays.toString((Character[]) val);

        if (type == String[].class)
            return Arrays.toString((String[]) val);

        return val != null ? val.toString() : null;
    }

    public static Object newInstance(Class<?> objectClass, String value) throws ParseException {
        if (objectClass == Boolean.TYPE)
            return Boolean.parseBoolean(value);

        if (objectClass == Boolean.class)
            return Boolean.valueOf(Boolean.parseBoolean(value));

        if (objectClass == Byte.TYPE)
            return Byte.parseByte(value);

        if (objectClass == Byte.class)
            return Byte.valueOf(Byte.parseByte(value));

        if (objectClass == Short.TYPE)
            return Short.parseShort(value);

        if (objectClass == Short.class)
            return Short.valueOf(Short.parseShort(value));

        if (objectClass == Integer.TYPE)
            return Integer.parseInt(value);

        if (objectClass == Integer.class)
            return Integer.valueOf(Integer.parseInt(value));

        if (objectClass == Long.TYPE)
            return Long.parseLong(value);

        if (objectClass == Long.class)
            return Long.valueOf(Long.parseLong(value));

        if (objectClass == Float.TYPE)
            return Float.parseFloat(value);

        if (objectClass == Float.class)
            return Float.valueOf(Float.parseFloat(value));

        if (objectClass == Double.TYPE)
            return Double.parseDouble(value);

        if (objectClass == Double.class)
            return Double.valueOf(Double.parseDouble(value));

        if (objectClass == Character.TYPE) {
            if (value.length() != 1)
                throw new ParseException("Nieprawidłowa wartość char", 0);
            return value.charAt(0);
        }
        if (objectClass == Character.class) {
            if (value.length() != 1)
                throw new ParseException("Nieprawidłowa wartość char", 0);
            return new Character(value.charAt(0));
        }
        if (objectClass == Date.class)
            return new SimpleDateFormat().parse(value);

        if (objectClass == String.class)
            return value;

        return null;
    }

    /**
     * Ustawia wartość pola danej instancji na podstawie nazwy
     */
    public static boolean setFieldValue(Object obj, String fieldName, String value) {
        try {
            Field field = obj.getClass().getField(fieldName);
            if (field != null)
                setFieldValue(field, obj, value);
            return true;
        } catch (Exception e) {
            Log.warning(e);
            return false;
        }

    }

    /**
     * obj: instancja obiektu lub klasa statyczna
     */
    public static void setFieldValue(Field field, Object obj, String value)
            throws IllegalArgumentException, IllegalAccessException, ParseException {
        if (field == null || obj == null)
            return;
        Class<?> type = field.getType();

        if (type == Boolean.TYPE)
            field.setBoolean(obj, Boolean.parseBoolean(value));

        if (type == Boolean.class)
            field.set(obj, Boolean.valueOf(Boolean.parseBoolean(value)));

        if (type == Byte.TYPE)
            field.setByte(obj, Byte.parseByte(value));

        if (type == Byte.class)
            field.set(obj, Byte.valueOf(Byte.parseByte(value)));

        if (type == Short.TYPE)
            field.setShort(obj, Short.parseShort(value));

        if (type == Short.class)
            field.set(obj, Short.valueOf(Short.parseShort(value)));

        if (type == Integer.TYPE)
            field.setInt(obj, Integer.parseInt(value));

        if (type == Integer.class)
            field.set(obj, Integer.valueOf(Integer.parseInt(value)));

        if (type == Long.TYPE)
            field.setLong(obj, Long.parseLong(value));

        if (type == Long.class)
            field.set(obj, Long.valueOf(Long.parseLong(value)));

        if (type == Float.TYPE)
            field.setFloat(obj, Float.parseFloat(sep(value)));

        if (type == Float.class)
            field.set(obj, Float.valueOf(Float.parseFloat(sep(value))));

        if (type == Double.TYPE)
            field.setDouble(obj, Double.parseDouble(sep(value)));

        if (type == Double.class)
            field.set(obj, Double.valueOf(Double.parseDouble(sep(value))));

        if (type == Character.TYPE) {
            if (value.length() != 1)
                throw new ParseException("Nieprawidłowa wartość char", 0);
            field.setChar(obj, value.charAt(0));
        }
        if (type == Character.class) {
            if (value.length() != 1)
                throw new ParseException("Nieprawidłowa wartość char", 0);
            field.set(obj, new Character(value.charAt(0)));
        }
        if (type == String.class)
            field.set(obj, value);
    }

    public static Object parseStr(Class<?> type, String value) throws ParseException {

        if (type == Boolean.TYPE)
            return Boolean.parseBoolean(value);
        if (type == Boolean.class)
            return Boolean.valueOf(Boolean.parseBoolean(value));
        if (type == Byte.TYPE)
            return Byte.parseByte(value);
        if (type == Byte.class)
            return Byte.valueOf(Byte.parseByte(value));
        if (type == Short.TYPE)
            return Short.parseShort(value);
        if (type == Short.class)
            return Short.valueOf(Short.parseShort(value));
        if (type == Integer.TYPE)
            return Integer.parseInt(value);
        if (type == Integer.class)
            return Integer.valueOf(Integer.parseInt(value));
        if (type == Long.TYPE)
            return Long.parseLong(value);
        if (type == Long.class)
            return Long.valueOf(Long.parseLong(value));
        if (type == Float.TYPE)
            return Float.parseFloat(sep(value));
        if (type == Float.class)
            return Float.valueOf(Float.parseFloat(sep(value)));
        if (type == Double.TYPE)
            return Double.parseDouble(sep(value));
        if (type == Double.class)
            return Double.valueOf(Double.parseDouble(sep(value)));
        if (type == Character.TYPE) {
            if (value.length() != 1)
                throw new ParseException("Nieprawidłowa wartość char", 0);
            return value.charAt(0);
        }
        if (type == Character.class) {
            if (value.length() != 1)
                throw new ParseException("Nieprawidłowa wartość char", 0);
            return new Character(value.charAt(0));
        }
        if (type == String.class)
            return value;

        return null;
    }

    /**
     * Konwersja separatora dziesietnego
     */
    private static String sep(String value) {
        if (value == null)
            return null;
        return value.replace(",", ".");
    }

    public static void setFieldArrayValue(Field field, Object obj, String[] values)
            throws IllegalArgumentException, IllegalAccessException, ParseException {
        if (field == null || obj == null)
            return;
        Class<?> type = field.getType();

        if (values == null) {
            field.set(obj, null);
            return;
        }

        if (type == boolean[].class) {
            boolean[] arr = new boolean[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Boolean.parseBoolean(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == Boolean[].class) {
            Boolean[] arr = new Boolean[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Boolean.parseBoolean(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == short[].class) {
            short[] arr = new short[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Short.parseShort(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == Short[].class) {
            Short[] arr = new Short[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Short.parseShort(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == int[].class) {
            int[] arr = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Integer.parseInt(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == Integer[].class) {
            Integer[] arr = new Integer[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Integer.parseInt(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == long[].class) {
            long[] arr = new long[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Long.parseLong(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == Long[].class) {
            Long[] arr = new Long[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Long.parseLong(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == float[].class) {
            float[] arr = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Float.parseFloat(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == Float[].class) {
            Float[] arr = new Float[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Float.parseFloat(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == double[].class) {
            double[] arr = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Double.parseDouble(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == Double[].class) {
            Double[] arr = new Double[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = Double.parseDouble(values[i]);
                field.set(obj, arr);
            }
            return;
        }
        if (type == char[].class) {
            char[] arr = new char[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i].length() != 1)
                    throw new ParseException("Nieprawidłowa wartość char", 0);
                arr[i] = values[i].charAt(0);
                field.set(obj, arr);
            }
            return;
        }
        if (type == Character[].class) {
            Character[] arr = new Character[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i].length() != 1)
                    throw new ParseException("Nieprawidłowa wartość char", 0);
                arr[i] = values[i].charAt(0);
                field.set(obj, arr);
            }
            return;
        }
        if (type == String[].class) {
            String[] arr = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                arr[i] = values[i];
                field.set(obj, arr);
            }
        }

    }

    /**
     * Czy dany typo może być null-em. W przypadku tablicy - czy elementy talicy
     * mogą być null-ami
     */
    public static boolean isSimple(Class<?> cls) {
        return isPrimitiveType(cls) || isPrimitiveObject(cls);
    }

    public static boolean isPrimitiveType(Class<?> cls) {
        return cls == Boolean.TYPE
                || cls == Byte.TYPE
                || cls == Short.TYPE
                || cls == Integer.TYPE
                || cls == Long.TYPE
                || cls == Float.TYPE
                || cls == Double.TYPE
                || cls == Character.TYPE;
    }

    public static boolean isPrimitiveObject(Class<?> cls) {
        return cls == Boolean.class
                || cls == Byte.class
                || cls == Short.class
                || cls == Integer.class
                || cls == Long.class
                || cls == Float.class
                || cls == Double.class
                || cls == Character.class
                || cls == String.class;
    }

    public static void checkStaticMethodInvoke(Method method, Class<?>... parameters) {
        int mods = method.getModifiers();

        if (parameters == null)
            parameters = new Class<?>[0];

        String name = method.getDeclaringClass().getName() + "." + method.getName();

        if (!Modifier.isStatic(mods))
            throw new RuntimeException("Metoda " + name + " nie jest statyczna");

        if (Modifier.isAbstract(mods))
            throw new RuntimeException("Metoda " + name + " jest abstrakcyjna");

        if (true)
            return;

        //todo: dodac obsluge opcjonalnychj parametrow metody
        Class<?>[] params = method.getParameterTypes();

        boolean ok = params.length == parameters.length;

        if (ok)
            for (int i = 0; i < parameters.length; i++)
                if (!parameters[0].equals(params[0])) {
                    ok = false;
                    break;
                }

        if (ok)
            return;

        if (parameters.length == 0)
            throw new RuntimeException("Metoda " + name
                    + " nie może przyjmować parametrów");

        Strings list = new Strings();
        for (Class<?> cls : parameters)
            list.add(cls.getName());

        throw new RuntimeException("Metoda " + name
                + " musi przyjmować parametry:\n" + list.toString(", \n"));

    }
}
