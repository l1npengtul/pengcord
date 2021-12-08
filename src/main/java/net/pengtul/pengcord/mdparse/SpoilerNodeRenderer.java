package net.pengtul.pengcord.mdparse;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SpoilerNodeRenderer implements NodeRenderer {

    public SpoilerNodeRenderer(DataHolder options) {}

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(Spoiler.class, this::render));
        return set;
    }

    private void render(Spoiler node, NodeRendererContext context, HtmlWriter html) {
        html.withAttr().tagIndent("details", ()-> context.renderChildren(node));
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new SpoilerNodeRenderer(options);
        }
    }
}
