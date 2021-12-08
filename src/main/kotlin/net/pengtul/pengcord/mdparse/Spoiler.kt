package net.pengtul.pengcord.mdparse

import com.vladsch.flexmark.util.ast.DelimitedNode
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import java.lang.StringBuilder

class Spoiler(op: BasedSequence?, txt: BasedSequence?, cls: BasedSequence?): Node(), DelimitedNode {

    init {
        this.openingMarker = op ?: BasedSequence.NULL
        this.text = txt ?: BasedSequence.NULL
        this.closingMarker = cls ?: BasedSequence.NULL
    }

    override fun getSegments(): Array<BasedSequence> {
        return mutableListOf(this.openingMarker, this.text, this.closingMarker).toTypedArray()
    }

    override fun getAstExtra(out: StringBuilder) {
        delimitedSegmentSpan(out, openingMarker, text, closingMarker, "text");
    }

    override fun getOpeningMarker(): BasedSequence {
        return openingMarker
    }

    override fun setOpeningMarker(openingMarker: BasedSequence?) {
        this.openingMarker = openingMarker ?: BasedSequence.NULL
    }

    override fun getText(): BasedSequence {
        return text
    }

    override fun setText(text: BasedSequence?) {
        this.text = text ?: BasedSequence.NULL
    }

    override fun getClosingMarker(): BasedSequence {
        return this.closingMarker
    }

    override fun setClosingMarker(closingMarker: BasedSequence?) {
        this.closingMarker = closingMarker ?: BasedSequence.NULL
    }
}