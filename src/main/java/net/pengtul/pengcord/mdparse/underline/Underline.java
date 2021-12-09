package net.pengtul.pengcord.mdparse.underline;

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class Underline extends Node implements DelimitedNode {
    protected BasedSequence openingMarker = BasedSequence.NULL;
    protected BasedSequence text = BasedSequence.NULL;
    protected BasedSequence closingMarker = BasedSequence.NULL;

    public Underline() {}

    public Underline(BasedSequence chars) {
        super(chars);
    }

    public Underline(BasedSequence openingMarker, BasedSequence text, BasedSequence closingMarker) {
        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.text = text;
        this.closingMarker = closingMarker;
    }

    @Override
    public BasedSequence getOpeningMarker() {
        return this.openingMarker;
    }

    @Override
    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    @Override
    public BasedSequence getText() {
        return this.text;
    }

    @Override
    public void setText(BasedSequence text) {
        this.text = text;
    }

    @Override
    public BasedSequence getClosingMarker() {
        return this.closingMarker;
    }

    @Override
    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[] { openingMarker, text, closingMarker };
    }
}
