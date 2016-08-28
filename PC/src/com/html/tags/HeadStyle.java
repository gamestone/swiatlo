package com.html.tags;

import com.html.core.StyleBuilder;
import com.html.core.Element;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class HeadStyle extends Element<HeadStyle> {

    public final HashMap<String, StyleBuilder> hstyles = new HashMap<>();

    public HeadStyle(Head parent) {
        super("style", parent);
        attr("rel", "stylesheet");
        attr("type", "text/css");
    }

    public String write(String space) {
        StringWriter sw = new StringWriter();
        for (Map.Entry<String, StyleBuilder> entry : hstyles.entrySet()) {
            String sel = entry.getKey();
            StyleBuilder st = entry.getValue();

            if (!builder.html.compactMode)
                sw.append("\n").append(space);
            sw.append(sel);
            if (!builder.html.compactMode)
                sw.append(" ");
            sw.append("{");

            st.writeHead(sw, space + "  ");

            if (!builder.html.compactMode)
                sw.append("\n").append(space);
            sw.append("}");
        }

        return sw.toString();

    }
}
