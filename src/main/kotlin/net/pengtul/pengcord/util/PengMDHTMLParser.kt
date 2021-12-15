package net.pengtul.pengcord.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.pengtul.pengcord.main.Main
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

class PengMDHTMLParser {
    private val validNodes = mutableListOf<String>()

    enum class TokenType(val decoration: TextDecoration) {
        Italic(
            TextDecoration.ITALIC
        ),
        Bold(
            TextDecoration.BOLD
        ),
        Underscore(
            TextDecoration.UNDERLINED
        ),
        Strikethrough(
            TextDecoration.STRIKETHROUGH
        ),
        Spoiler(
            TextDecoration.OBFUSCATED
        ),
    }

    init {
        validNodes.add("p")
        validNodes.add("em")
        validNodes.add("strong")
        validNodes.add("u")
        validNodes.add("del")
        validNodes.add("details")
        validNodes.add("code")
        validNodes.add("blockquote")
    }

    class NodeFinder: NodeVisitor {
        val textComponent = Component.text()
        private val formattingStack = mutableListOf<TextDecoration>()
        private var insideQuote = false
        private var insideCodeBlock = false
        private var insideSpoiler = false
        private var lastExitName = ""
        private var spoilerQuote = Component.text()
        private var spoiler = Component.text()
        private var blockQuoteQuote = Component.text()

        override fun head(node: Node, depth: Int) {
            when (node.nodeName().lowercase()) {
                "em" -> {
                    formattingStack.add(TokenType.Italic.decoration)
                }
                "strong" -> {
                    formattingStack.add(TokenType.Bold.decoration)
                }
                "u" -> {
                    formattingStack.add(TokenType.Underscore.decoration)
                }
                "del" -> {
                    formattingStack.add(TokenType.Strikethrough.decoration)
                }
                "details" -> {
                    insideSpoiler = true
                    formattingStack.add(TokenType.Spoiler.decoration)
                }
                "code" -> {
                    insideCodeBlock = true
                }
                "blockquote" -> {
                    insideQuote = true
                }
                "#text" -> {
                    if (node is TextNode && node.wholeText.isNotBlank()) {
                        val compToAdd = Component.text()

                        node.wholeText.lines().forEach {
                            val lineComponent = Component.text(it)
                            if (it.startsWith(">")) {
                                lineComponent.color(NamedTextColor.GREEN)
                            }
                            compToAdd.append(lineComponent)
                        }

                        if (insideCodeBlock) {
                            compToAdd.style(Style.style(TextDecoration.ITALIC))
                            compToAdd.color(NamedTextColor.DARK_GRAY)
                        } else if (insideSpoiler) {
                            formattingStack.forEach {
                                if (it != TextDecoration.OBFUSCATED) {
                                    compToAdd.decorate(it)
                                }
                            }
                        } else {
                            formattingStack.forEach {
                                if (it != TextDecoration.OBFUSCATED) {
                                    compToAdd.decorate(it)
                                } else {
                                    compToAdd.style(Style.style(TextDecoration.OBFUSCATED))
                                }
                            }
                        }
                        val finalBuilt = compToAdd.build()
                        if (insideSpoiler) {
                            spoiler.append(finalBuilt)
                            spoilerQuote.append(finalBuilt)
                        } else if (insideQuote) {
                            blockQuoteQuote.append(finalBuilt)
                        } else {
                            textComponent.append(finalBuilt)
                        }
                    }
                }
                else -> {
                }
            }
        }

        override fun tail(node: Node, depth: Int) {
            when (node.nodeName()) {
                "em", "strong", "u", "del", "details" -> {
                    formattingStack.removeLastOrNull()
                }
                else -> {

                }
            }
            when (lastExitName) {
                "code" -> {
                    insideCodeBlock = false
                }
                "blockquote" -> {
                    insideQuote = false
                    textComponent.append(this.blockQuoteQuote.color(NamedTextColor.GREEN))
                    this.blockQuoteQuote = Component.text()
                }
                "details" -> {
                    insideSpoiler = false
                    val spoiler = Component.text()
                        .append(this.spoilerQuote)
                        .style(Style.style(NamedTextColor.WHITE))
                        .build()
                    textComponent.append(
                        this.spoiler
                            .decorate(TextDecoration.OBFUSCATED)
                            .hoverEvent(HoverEvent.showText(spoiler)))
                    this.spoiler = Component.text()
                    this.spoilerQuote = Component.text()
                }
            }

            lastExitName = node.nodeName()
        }
    }

//    fun preProcessHtml(html: String): String {
//        Main.serverLogger.info(html)
//        val toReplaceWith = codeBlockRegex.findAll(html).map {
//            val match = it.value
//            match.replace(this.italicRegex, "*")
//            match.replace(this.boldRegex, "**")
//            match.replace(this.underscoreRegex, "__")
//            match.replace(this.strikethroughRegex, "~~")
//            match.replace(this.spoilerRegex, "||")
//            return@map match
//        }.toList()
//        val finds = codeBlockRegex.findAll(html).map { it.range }.toList()
//        var formatted = html
//        finds.zip(toReplaceWith).forEach {
//            formatted = formatted.replaceRange(it.first, it.second)
//        }
//        Main.serverLogger.info(formatted)
//        return formatted
//    }

    fun parse(document: Document): Component {
        val traverser = NodeFinder()
        document.traverse(traverser)
        return traverser.textComponent.build()
    }
}
