package http;

import com.html.tags.*;
import driver.channels.Group;
import java.io.IOException;

public class Main extends Page {

    public Main(HttpReq http) throws IOException {
        super(http);

    }

    @Override
    public void processRequest() throws Exception {
        html.head.linkCSS("styles.css");
        html.head.linkJavaScript("utils.js");
        html.head.linkJavaScript("service.js");
        html.head.linkJavaScript("scripts.js");
        html.head.linkJavaScript("picker/jquery-1.7.1.min.js");
        html.head.linkJavaScript("picker/jquery.wheelcolorpicker.js");
        html.head.linkCSS("picker/css/wheelcolorpicker.css");

        Body body = html.body;

        Tag dGroups = body.div().cls("groups");

        for (Group gr : Group.all) {
            Tag div = dGroups.div().cls("group").id("G" + gr.id);
            div.attr("gid", gr.id);

            Tag hdr = div.div().cls("group-header");

            hdr.span().text(gr.getName()).cls("group-header-name");
            hdr.span().id("lbl" + gr.key + "val").cls("group-header-value");

            div.div().cls("pre-slider").div()
                    .cls("slider")
                    .id("slider" + gr.key)
                    .attr("channel", gr.key).style.left((100 * gr.getValue() / 4095) + "%");

        }

        body.button().text("Aktualizuj grupy").onClick("btnUpdateGroupsClick();");

        body.input().type(InputType.text).id("rgb");
        
     }

}
