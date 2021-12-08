package net.pengtul.pengcord.mdparse

import com.vladsch.flexmark.ext.gfm.strikethrough.internal.StrikethroughJiraRenderer
import com.vladsch.flexmark.ext.gfm.strikethrough.internal.StrikethroughNodeRenderer
import com.vladsch.flexmark.ext.gfm.strikethrough.internal.StrikethroughYouTrackRenderer
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.data.NullableDataKey


class SpoilerExtension: Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    fun create(): SpoilerExtension {
        return SpoilerExtension()
    }

    override fun parserOptions(options: MutableDataHolder?) {
    }

    override fun extend(parserBuilder: Parser.Builder?) {
        parserBuilder?.customDelimiterProcessor(SpoilerDelimiterProcessor())
    }

    override fun rendererOptions(options: MutableDataHolder) {
    }

    override fun extend(htmlRendererBuilder: HtmlRenderer.Builder, rendererType: String) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(SpoilerNodeRenderer.Companion.Factory())
        }
    }
}