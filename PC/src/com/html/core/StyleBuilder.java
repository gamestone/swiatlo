package com.html.core;

import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public final class StyleBuilder {

    public final Map<String, String> selectors = new TreeMap<>();
    private final Node tag;
    private final HtmlBuilder builder;

    //=======================================================================================
    private StyleBuilder setStyle(final String name, final String value,
            final String... def) {
        selectors.put(name, value);
        return this;
    }

    public final StyleBuilder animation(final String value) {
        return setStyle("animation", value);
    }

    public final StyleBuilder animationName(final String value) {
        return setStyle("animation-name", value);
    }

    public final StyleBuilder animationDuration(final String value) {
        return setStyle("animation-duration", value);
    }

    public final StyleBuilder animationTimingFunction(final String value) {
        return setStyle("animation-timing-function", value);
    }

    public final StyleBuilder animationDelay(final String value) {
        return setStyle("animation-delay", value);
    }

    public final StyleBuilder animationIterationCount(final String value) {
        return setStyle("animation-iteration-count", value);
    }

    public final StyleBuilder animationDirection(final String value) {
        return setStyle("animation-direction", value);
    }

    public final StyleBuilder animationPlayState(final String value) {
        return setStyle("animation-play-state", value);
    }

    public final StyleBuilder appearance(final String value) {
        return setStyle("appearance", value);
    }

    public final StyleBuilder backfaceVisibility(final String value) {
        return setStyle("backface-visibility", value);
    }

    public final StyleBuilder background(final String value) {
        return setStyle("background", value);
    }

    public final StyleBuilder backgroundAttachment(final String value) {
        return setStyle("background-attachment", value);
    }

    public final StyleBuilder backgroundColor(final String value) {
        return setStyle("background-color", value);
    }

    public final StyleBuilder backgroundImage(final String value) {
        return setStyle("background-image", value);
    }

    public final StyleBuilder backgroundPosition(final String value) {
        return setStyle("background-position", value);
    }

    public final StyleBuilder backgroundRepeat(final String value) {
        return setStyle("background-repeat", value);
    }

    public final StyleBuilder backgroundClip(final String value) {
        return setStyle("background-clip", value);
    }

    public final StyleBuilder backgroundOrigin(final String value) {
        return setStyle("background-origin", value);
    }

    public final StyleBuilder backgroundSize(final String value) {
        return setStyle("background-size", value);
    }

    public final StyleBuilder border(final String value) {
        return setStyle("border", value);
    }

    public final StyleBuilder borderBottom(final String value) {
        return setStyle("border-bottom", value);
    }

    public final StyleBuilder borderBottomColor(final String value) {
        return setStyle("border-bottom-color", value);
    }

    public final StyleBuilder borderBottomStyle(final String value) {
        return setStyle("border-bottom-style", value);
    }

    public final StyleBuilder borderBottomWidth(final String value) {
        return setStyle("border-bottom-width", value);
    }

    public final StyleBuilder borderCollapse(final String value) {
        return setStyle("border-collapse", value);
    }

    public final StyleBuilder borderColor(final String value) {
        return setStyle("border-color", value);
    }

    public final StyleBuilder borderLeft(final String value) {
        return setStyle("border-left", value);
    }

    public final StyleBuilder borderLeftColor(final String value) {
        return setStyle("border-left-color", value);
    }

    public final StyleBuilder borderLeftStyle(final String value) {
        return setStyle("border-left-style", value);
    }

    public final StyleBuilder borderLeftWidth(final String value) {
        return setStyle("border-left-width", value);
    }

    public final StyleBuilder borderRight(final String value) {
        return setStyle("border-right", value);
    }

    public final StyleBuilder borderRightColor(final String value) {
        return setStyle("border-right-color", value);
    }

    public final StyleBuilder borderRightStyle(final String value) {
        return setStyle("border-right-style", value);
    }

    public final StyleBuilder borderRightWidth(final String value) {
        return setStyle("border-right-width", value);
    }

    public final StyleBuilder borderSpacing(final String value) {
        return setStyle("border-spacing", value);
    }

    public final StyleBuilder borderStyle(final String value) {
        return setStyle("border-style", value);
    }

    public final StyleBuilder borderTop(final String value) {
        return setStyle("border-top", value);
    }

    public final StyleBuilder borderTopColor(final String value) {
        return setStyle("border-top-color", value);
    }

    public final StyleBuilder borderTopStyle(final String value) {
        return setStyle("border-top-style", value);
    }

    public final StyleBuilder borderTopWidth(final String value) {
        return setStyle("border-top-width", value);
    }

    public final StyleBuilder borderWidth(final String value) {
        return setStyle("border-width", value);
    }

    public final StyleBuilder borderBottomLeftRadius(final String value) {
        return setStyle("border-bottom-left-radius", value);
    }

    public final StyleBuilder borderBottomRightRadius(final String value) {
        return setStyle("border-bottom-right-radius", value);
    }

    public final StyleBuilder borderImage(final String value) {
        return setStyle("border-image", value);
    }

    public final StyleBuilder borderImageOutset(final String value) {
        return setStyle("border-image-outset", value);
    }

    public final StyleBuilder borderImageRepeat(final String value) {
        return setStyle("border-image-repeat", value);
    }

    public final StyleBuilder borderImageSlice(final String value) {
        return setStyle("border-image-slice", value);
    }

    public final StyleBuilder borderImageSource(final String value) {
        return setStyle("border-image-source", value);
    }

    public final StyleBuilder borderImageWidth(final String value) {
        return setStyle("border-image-width", value);
    }

    public final StyleBuilder borderRadius(final String value) {
        return setStyle("border-radius", value);
    }

    public final StyleBuilder borderTopLeftRadius(final String value) {
        return setStyle("border-top-left-radius", value);
    }

    public final StyleBuilder borderTopRightRadius(final String value) {
        return setStyle("border-top-right-radius", value);
    }

    public final StyleBuilder bottom(final String value) {
        return setStyle("bottom", value);
    }

    public final StyleBuilder box(final String value) {
        return setStyle("box", value);
    }

    public final StyleBuilder boxAlign(final String value) {
        return setStyle("box-align", value);
    }

    public final StyleBuilder boxDirection(final String value) {
        return setStyle("box-direction", value);
    }

    public final StyleBuilder boxFlex(final String value) {
        return setStyle("box-flex", value);
    }

    public final StyleBuilder boxFlexGroup(final String value) {
        return setStyle("box-flex-group", value);
    }

    public final StyleBuilder boxLines(final String value) {
        return setStyle("box-lines", value);
    }

    public final StyleBuilder boxOrdinalGroup(final String value) {
        return setStyle("box-ordinal-group", value);
    }

    public final StyleBuilder boxOrient(final String value) {
        return setStyle("box-orient", value);
    }

    public final StyleBuilder boxPack(final String value) {
        return setStyle("box-pack", value);
    }

    public final StyleBuilder boxSizing(final String value) {
        return setStyle("box-sizing", value);
    }

    public final StyleBuilder boxShadow(final String value) {
        return setStyle("box-shadow", value);
    }

    public final StyleBuilder captionSide(final String value) {
        return setStyle("caption-side", value);
    }

    public final StyleBuilder clear(final String value) {
        return setStyle("clear", value);
    }

    public final StyleBuilder clip(final String value) {
        return setStyle("clip", value);
    }

    public final StyleBuilder color(final String value) {
        return setStyle("color", value);
    }

    public final StyleBuilder column(final String value) {
        return setStyle("column", value);
    }

    public final StyleBuilder columnCount(final String value) {
        return setStyle("column-count", value);
    }

    public final StyleBuilder columnFill(final String value) {
        return setStyle("column-fill", value);
    }

    public final StyleBuilder columnGap(final String value) {
        return setStyle("column-gap", value);
    }

    public final StyleBuilder columnRule(final String value) {
        return setStyle("column-rule", value);
    }

    public final StyleBuilder columnRuleColor(final String value) {
        return setStyle("column-rule-color", value);
    }

    public final StyleBuilder columnRuleStyle(final String value) {
        return setStyle("column-rule-style", value);
    }

    public final StyleBuilder columnRuleWidth(final String value) {
        return setStyle("column-rule-width", value);
    }

    public final StyleBuilder columnSpan(final String value) {
        return setStyle("column-span", value);
    }

    public final StyleBuilder columnWidth(final String value) {
        return setStyle("column-width", value);
    }

    public final StyleBuilder columns(final String value) {
        return setStyle("columns", value);
    }

    public final StyleBuilder content(final String value) {
        return setStyle("content", value);
    }

    public final StyleBuilder counterIncrement(final String value) {
        return setStyle("counter-increment", value);
    }

    public final StyleBuilder counterReset(final String value) {
        return setStyle("counter-reset", value);
    }

    public final StyleBuilder cursor(final String value) {
        return setStyle("cursor", value);
    }

    public final StyleBuilder direction(final String value) {
        return setStyle("direction", value);
    }

    public final StyleBuilder display(final String value) {
        return setStyle("display", value);
    }

    public final StyleBuilder emptyCells(final String value) {
        return setStyle("empty-cells", value);
    }

    public final StyleBuilder float_(final String value) {
        return setStyle("float", value);
    }

    public final StyleBuilder font(final String value) {
        return setStyle("font", value);
    }

    public final StyleBuilder fontFamily(final String value) {
        return setStyle("font-family", value);
    }

    public final StyleBuilder fontFamilyCourierNew() {
        return setStyle("font-family", "\"Courier New\", monospace");
    }

    public final StyleBuilder fontSize(final String value) {
        return setStyle("font-size", value);
    }

    public final StyleBuilder fontStyle(final String value) {
        return setStyle("font-style", value);
    }

    public final StyleBuilder fontVariant(final String value) {
        return setStyle("font-variant", value);
    }

    public final StyleBuilder fontWeight(final String value) {
        return setStyle("font-weight", value);
    }

    public final StyleBuilder _fontFace(final String value) {
        return setStyle("@font-face", value);
    }

    public final StyleBuilder fontSizeAdjust(final String value) {
        return setStyle("font-size-adjust", value);
    }

    public final StyleBuilder fontStretch(final String value) {
        return setStyle("font-stretch", value);
    }

    public final StyleBuilder gridColumns(final String value) {
        return setStyle("grid-columns", value);
    }

    public final StyleBuilder gridRows(final String value) {
        return setStyle("grid-rows", value);
    }

    public final StyleBuilder hangingPunctuation(final String value) {
        return setStyle("hanging-punctuation", value);
    }

    public final StyleBuilder height(final String value) {
        return setStyle("height", value);
    }

    public final StyleBuilder icon(final String value) {
        return setStyle("icon", value);
    }

    public final StyleBuilder _keyframes(final String value) {
        return setStyle("@keyframes", value);
    }

    public final StyleBuilder left(final String value) {
        return setStyle("left", value);
    }

    public final StyleBuilder letterSpacing(final String value) {
        return setStyle("letter-spacing", value);
    }

    public final StyleBuilder lineHeight(final String value) {
        return setStyle("line-height", value);
    }

    public final StyleBuilder listStyle(final String value) {
        return setStyle("list-style", value);
    }

    public final StyleBuilder listStyleImage(final String value) {
        return setStyle("list-style-image", value);
    }

    public final StyleBuilder listStylePosition(final String value) {
        return setStyle("list-style-position", value);
    }

    public final StyleBuilder listStyleType(final String value) {
        return setStyle("list-style-type", value);
    }

    public final StyleBuilder margin(final String value) {
        return setStyle("margin", value);
    }

    public final StyleBuilder marginBottom(final String value) {
        return setStyle("margin-bottom", value);
    }

    public final StyleBuilder marginLeft(final String value) {
        return setStyle("margin-left", value);
    }

    public final StyleBuilder marginRight(final String value) {
        return setStyle("margin-right", value);
    }

    public final StyleBuilder marginTop(final String value) {
        return setStyle("margin-top", value);
    }

    public final StyleBuilder maxHeight(final String value) {
        return setStyle("max-height", value);
    }

    public final StyleBuilder maxWidth(final String value) {
        return setStyle("max-width", value);
    }

    public final StyleBuilder minHeight(final String value) {
        return setStyle("min-height", value);
    }

    public final StyleBuilder minWidth(final String value) {
        return setStyle("min-width", value);
    }

    public final StyleBuilder nav(final String value) {
        return setStyle("nav", value);
    }

    public final StyleBuilder navDown(final String value) {
        return setStyle("nav-down", value);
    }

    public final StyleBuilder navIndex(final String value) {
        return setStyle("nav-index", value);
    }

    public final StyleBuilder navLeft(final String value) {
        return setStyle("nav-left", value);
    }

    public final StyleBuilder navRight(final String value) {
        return setStyle("nav-right", value);
    }

    public final StyleBuilder navUp(final String value) {
        return setStyle("nav-up", value);
    }

    public final StyleBuilder opacity(final String value) {
        return setStyle("opacity", value);
    }

    public final StyleBuilder outline(final String value) {
        return setStyle("outline", value);
    }

    public final StyleBuilder outlineColor(final String value) {
        return setStyle("outline-color", value);
    }

    public final StyleBuilder outlineOffset(final String value) {
        return setStyle("outline-offset", value);
    }

    public final StyleBuilder outlineStyle(final String value) {
        return setStyle("outline-style", value);
    }

    public final StyleBuilder outlineWidth(final String value) {
        return setStyle("outline-width", value);
    }

    public final StyleBuilder overflow(final String value) {
        return setStyle("overflow", value);
    }

    public final StyleBuilder overflowX(final String value) {
        return setStyle("overflow-x", value);
    }

    public final StyleBuilder overflowY(final String value) {
        return setStyle("overflow-y", value);
    }

    public final StyleBuilder padding(final String value) {
        return setStyle("padding", value);
    }

    public final StyleBuilder paddingBottom(final String value) {
        return setStyle("padding-bottom", value);
    }

    public final StyleBuilder paddingLeft(final String value) {
        return setStyle("padding-left", value);
    }

    public final StyleBuilder paddingRight(final String value) {
        return setStyle("padding-right", value);
    }

    public final StyleBuilder paddingTop(final String value) {
        return setStyle("padding-top", value);
    }

    public final StyleBuilder pageBreak(final String value) {
        return setStyle("page-break", value);
    }

    public final StyleBuilder pageBreakAfter(final String value) {
        return setStyle("page-break-after", value);
    }

    public final StyleBuilder pageBreakBefore(final String value) {
        return setStyle("page-break-before", value);
    }

    public final StyleBuilder pageBreakInside(final String value) {
        return setStyle("page-break-inside", value);
    }

    public final StyleBuilder perspective(final String value) {
        return setStyle("perspective", value);
    }

    public final StyleBuilder perspectiveOrigin(final String value) {
        return setStyle("perspective-origin", value);
    }

    public final StyleBuilder position(final String value) {
        return setStyle("position", value);
    }

    public final StyleBuilder punctuationTrim(final String value) {
        return setStyle("punctuation-trim", value);
    }

    public final StyleBuilder quotes(final String value) {
        return setStyle("quotes", value);
    }

    public final StyleBuilder resize(final String value) {
        return setStyle("resize", value);
    }

    public final StyleBuilder right(final String value) {
        return setStyle("right", value);
    }

    public final StyleBuilder rotation(final String value) {
        return setStyle("rotation", value);
    }

    public final StyleBuilder rotationPoint(final String value) {
        return setStyle("rotation-point", value);
    }

    public final StyleBuilder tableLayout(final String value) {
        return setStyle("table-layout", value);
    }

    public final StyleBuilder target(final String value) {
        return setStyle("target", value);
    }

    public final StyleBuilder targetName(final String value) {
        return setStyle("target-name", value);
    }

    public final StyleBuilder targetNew(final String value) {
        return setStyle("target-new", value);
    }

    public final StyleBuilder targetPosition(final String value) {
        return setStyle("target-position", value);
    }

    public final StyleBuilder text(final String value) {
        return setStyle("text", value);
    }

    public final StyleBuilder textAlign(final String value) {
        return setStyle("text-align", value);
    }

    public final StyleBuilder textDecoration(final String value) {
        return setStyle("text-decoration", value,
                "none", "underline", "overline", "line-through", "blink", "inherit");
    }

    public final StyleBuilder textIndent(final String value) {
        return setStyle("text-indent", value);
    }

    public final StyleBuilder textJustify(final String value) {
        return setStyle("text-justify", value);
    }

    public final StyleBuilder textOutline(final String value) {
        return setStyle("text-outline", value);
    }

    public final StyleBuilder textOverflow(final String value) {
        return setStyle("text-overflow", value);
    }

    public final StyleBuilder textShadow(final String value) {
        return setStyle("text-shadow", value);
    }

    public final StyleBuilder textTransform(final String value) {
        return setStyle("text-transform", value);
    }

    public final StyleBuilder textWrap(final String value) {
        return setStyle("text-wrap", value);
    }

    public final StyleBuilder top(final String value) {
        return setStyle("top", value);
    }

    public final StyleBuilder transform(final String value) {
        return setStyle("transform", value);
    }

    public final StyleBuilder transformOrigin(final String value) {
        return setStyle("transform-origin", value);
    }

    public final StyleBuilder transformStyle(final String value) {
        return setStyle("transform-style", value);
    }

    public final StyleBuilder transition(final String value) {
        return setStyle("transition", value);
    }

    public final StyleBuilder transitionProperty(final String value) {
        return setStyle("transition-property", value);
    }

    public final StyleBuilder transitionDuration(final String value) {
        return setStyle("transition-duration", value);
    }

    public final StyleBuilder transitionTimingFunction(final String value) {
        return setStyle("transition-timing-function", value);
    }

    public final StyleBuilder transitionDelay(final String value) {
        return setStyle("transition-delay", value);
    }

    public final StyleBuilder userSelect(final String value) {
        setStyle("-webkit-user-select", value, "text", "none");
        setStyle("-khtml-user-select", value, "text", "none");
        setStyle("-moz-user-select", value, "text", "none");
        setStyle("-o-user-select", value, "text", "none");
        return setStyle("-ms-user-select", value, "text", "none");
    }

    public final StyleBuilder verticalAlign(final String value) {
        return setStyle("vertical-align", value);
    }

    public final StyleBuilder visibility(final String value) {
        return setStyle("visibility", value);
    }

    public final StyleBuilder width(final String value) {
        return setStyle("width", value);
    }

    public final StyleBuilder whiteSpace(final String value) {
        return setStyle("white-space", value, "nowrap");
    }

    public final StyleBuilder wordSpacing(final String value) {
        return setStyle("word-spacing", value);
    }

    public final StyleBuilder wordBreak(final String value) {
        return setStyle("word-break", value);
    }

    public final StyleBuilder wordWrap(final String value) {
        return setStyle("word-wrap", value);
    }

    public final StyleBuilder zIndex(final String value) {
        return setStyle("z-index", value);
    }

    //=====================================================================================
    public StyleBuilder(final HtmlBuilder builder, final Node tag) {
        this.builder = builder;
        this.tag = tag;
    }

    public void writeHead(StringWriter sw, String space) {

        for (Entry<String, String> sel : selectors.entrySet()) {
            if (!builder.compactMode)
                sw.append("\n").append(space);
            sw.append(sel.getKey()).append(":");
            if (!builder.compactMode)
                sw.append(" ");
            sw.append(sel.getValue()).append(";");
        }
    }

    @Override
    public String toString() {
        return selectors.isEmpty() ? "" : write("");
    }

    public String write(String space) {
        if (selectors.isEmpty())
            return null;

        StringWriter sw = new StringWriter();
        boolean rn = false;
        for (Entry<String, String> sel : selectors.entrySet()) {
            if (rn)
                sw.append(" ");
            sw.append(sel.getKey())
                    .append(": ").append(sel.getValue()).append(";");
            rn = !builder.compactMode;
        }

        return sw.toString();
    }
}
