package com.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Map;

public abstract class JSON {

    public static JElement serialize(Object object) {
        return serialize(object, new GsonBuilder());
    }

    public static JElement serialize(Object object, GsonBuilder builder) {

        if (object instanceof JElement)
            return (JElement) object;

        if ((object instanceof Boolean)
                || (object instanceof Number)
                || (object instanceof String))
            return new JValue(object);

        if (object instanceof Iterable) {
            JArray arr = new JArray();
            for (Object o : (Iterable) object)
                arr.add(o);

            return arr;
        }

        if (builder == null)
            builder = new GsonBuilder();

        final Gson gson = builder.create();
        JsonElement el = gson.toJsonTree(object);
        return convert(el);
    }

    public static JElement parse(String in) {
        return parse(new BufferedReader(new StringReader(in)));
    }

    public static JElement parse(InputStream in) {
        return parse(new BufferedReader(
                new InputStreamReader(in, Charset.forName("UTF-8"))));
    }

    @SuppressWarnings("unchecked")
    public static JElement parse(Reader reader) {
        JsonReader jreader = new JsonReader(reader);
        jreader.setLenient(true);// mniejsze restrykcje dotyczace skladni

        JsonElement je = new JsonParser().parse(jreader);
        if (je == null)
            return null;

        if (je.isJsonNull())
            return new JValue(null);

        return convert(je);
    }

    private static Object getPrimitive(JsonPrimitive val) {
        if (val.isBoolean())
            return val.getAsBoolean();
        else
            if (val.isNumber())
                return val.getAsNumber();
            else
                if (val.isString())
                    return val.getAsString();
        return null;
    }

    public static JElement convert(JsonElement source) {

        if (source == null)
            return null;

        if (source instanceof JsonNull)
            return new JValue(null);

        if (source instanceof JsonPrimitive)
            return new JValue(getPrimitive(source.getAsJsonPrimitive()));

        if (source instanceof JsonObject) {

            JObject obj = new JObject();

            for (Map.Entry<String, JsonElement> ee : source.getAsJsonObject().entrySet())
                obj.addElement(ee.getKey(),
                        convert(ee.getValue()), false);

            return obj;
        }

        if (source instanceof JsonArray) {
            JArray arr = new JArray();
            for (JsonElement el : source.getAsJsonArray())
                arr.addElement(null, convert(el), false);
            return arr;
        }

        return null;
    }

}
