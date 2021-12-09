package net.pengtul.pengcord.mdparse.underline;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

public class UnderlineExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    private UnderlineExtension() {
    }

    public static UnderlineExtension create() {
        return new UnderlineExtension();
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(HtmlRenderer.@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new UnderlineNodeRenderer.Factory());
        }
    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new UnderlineDelimiterProcessor());
    }
}
