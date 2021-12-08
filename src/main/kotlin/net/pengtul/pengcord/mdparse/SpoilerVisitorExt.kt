package net.pengtul.pengcord.mdparse

import com.vladsch.flexmark.util.ast.VisitHandler

class SpoilerVisitorExt {
    companion object {
        fun <V : SpoilerVisitor> VISIT_HANDLERS(visitor: V): Array<VisitHandler<*>> {
            return arrayOf(
                VisitHandler(
                    Spoiler::class.java
                ) { node: Spoiler? ->
                    if (node != null) {
                        visitor.visit(node)
                    }
                })
        }
    }
}