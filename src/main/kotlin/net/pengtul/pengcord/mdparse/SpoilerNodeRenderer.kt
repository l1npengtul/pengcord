package net.pengtul.pengcord.mdparse

import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.NodeRenderer
import com.vladsch.flexmark.html.renderer.NodeRendererContext
import com.vladsch.flexmark.html.renderer.NodeRendererFactory
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler
import com.vladsch.flexmark.util.data.DataHolder

class SpoilerNodeRenderer(dataHolder: DataHolder): NodeRenderer {
    override fun getNodeRenderingHandlers(): MutableSet<NodeRenderingHandler<*>> {
        val set: HashSet<NodeRenderingHandler<*>> = HashSet()
        set.add(NodeRenderingHandler(Spoiler::class.java, this::render))
        return set
    }

    private fun render(node: Spoiler, context: NodeRendererContext, html: HtmlWriter) {
        html.withAttr().tagIndent("details") {
            context.renderChildren(node)
        }
    }

    companion object {
        class Factory: NodeRendererFactory {
            override fun apply(options: DataHolder): NodeRenderer {
                return SpoilerNodeRenderer(options)
            }

        }
    }
}