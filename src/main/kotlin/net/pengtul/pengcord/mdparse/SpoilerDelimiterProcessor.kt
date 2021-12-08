package net.pengtul.pengcord.mdparse

import com.vladsch.flexmark.parser.InlineParser
import com.vladsch.flexmark.parser.core.delimiter.Delimiter
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor
import com.vladsch.flexmark.parser.delimiter.DelimiterRun
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence

class SpoilerDelimiterProcessor: DelimiterProcessor {
    override fun getOpeningCharacter(): Char {
        return '|'
    }

    override fun getClosingCharacter(): Char {
        return '|'
    }

    override fun getMinLength(): Int {
        return 2
    }

    override fun getDelimiterUse(opener: DelimiterRun?, closer: DelimiterRun?): Int {
        opener?.let {
            return if (it.length() >= 2 && it.length() >= 2) {
                2
            } else {
                0
            }
        }
        return 0
    }

    override fun process(opener: Delimiter?, closer: Delimiter?, delimitersUsed: Int) {
        val spoiler = Spoiler(opener?.getTailChars(delimitersUsed), BasedSequence.NULL, closer?.getLeadChars(2))
        opener?.moveNodesBetweenDelimitersTo(spoiler, closer)
    }

    override fun unmatchedDelimiterNode(inlineParser: InlineParser?, delimiter: DelimiterRun?): Node? {
        return null
    }

    override fun canBeOpener(
        before: String?,
        after: String?,
        leftFlanking: Boolean,
        rightFlanking: Boolean,
        beforeIsPunctuation: Boolean,
        afterIsPunctuation: Boolean,
        beforeIsWhitespace: Boolean,
        afterIsWhiteSpace: Boolean
    ): Boolean {
        return leftFlanking
    }

    override fun canBeCloser(
        before: String?,
        after: String?,
        leftFlanking: Boolean,
        rightFlanking: Boolean,
        beforeIsPunctuation: Boolean,
        afterIsPunctuation: Boolean,
        beforeIsWhitespace: Boolean,
        afterIsWhiteSpace: Boolean
    ): Boolean {
        return rightFlanking
    }

    override fun skipNonOpenerCloser(): Boolean {
        return false
    }
}