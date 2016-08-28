package com.html.tags;

import com.Utils;
import com.html.core.Element;
import com.html.core.Interfaces.INonContentTag;
import com.html.core.Interfaces.ITagListenner;
import com.html.core.Node;
import java.util.LinkedList;
import java.util.List;

public class DataList extends Element<DataList> implements INonContentTag {

    public final List<String> list = new LinkedList<>();
    public Node parentInput;

    public DataList(Node parent, Node parentInput) {
        super("datalist", parent);
        this.parentInput = parentInput;

        if (parentInput == null)
            return;

        id(Utils.randomId(8));
        parentInput.attr("list", getId());

        addListenner(new ITagListenner() {
            @Override
            public boolean onBeforeBuildTag(Node tag) {
                tag.clear();
                for (String s : list)
                    tag.tag("option").attr("value", s);

                return true;
            }
        });

    }
}
