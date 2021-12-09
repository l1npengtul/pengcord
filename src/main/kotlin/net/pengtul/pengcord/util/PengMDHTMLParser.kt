package net.pengtul.pengcord.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.pengtul.pengcord.main.Main
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

class PengMDHTMLParser() {
    private val validNodes = mutableListOf<String>()

    enum class TokenType(val style: Style) {
        Normal(Style.style(NamedTextColor.WHITE)),
        Italic(
            Style.style()
                .decorate(TextDecoration.ITALIC)
                .color(NamedTextColor.WHITE)
                .build()
        ),
        Bold(
            Style.style()
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.WHITE)
                .build()
        ),
        Underscore(
            Style.style()
                .decorate(TextDecoration.UNDERLINED)
                .color(NamedTextColor.WHITE)
                .build()
        ),
        Strikethrough(
            Style.style()
                .decorate(TextDecoration.STRIKETHROUGH)
                .color(NamedTextColor.WHITE)
                .build()
        ),
        Spoiler(
            Style.style()
                .decorate(TextDecoration.OBFUSCATED)
                .color(NamedTextColor.WHITE)
                .build()
        ),
        CodeBlock(
            Style.style()
                .color(NamedTextColor.DARK_GRAY)
                .build()
        ),
        Quote(
            Style.style()
                .color(NamedTextColor.GREEN)
                .build()
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
        private val formattingStack = mutableListOf<Style>()
        private var insideQuote = false
        private var insideCodeBlock = false
        private var lastExitName = ""


        override fun head(node: Node, depth: Int) {
            Main.serverLogger.info("Entering node ${node.nodeName()}, ${node}")
            when (node.nodeName().lowercase()) {
                "p" -> {
                    formattingStack.add(TokenType.Normal.style)
                }
                "em" -> {
                    formattingStack.add(TokenType.Italic.style)
                }
                "strong" -> {
                    formattingStack.add(TokenType.Bold.style)
                }
                "u" -> {
                    formattingStack.add(TokenType.Underscore.style)
                }
                "del" -> {
                    formattingStack.add(TokenType.Strikethrough.style)
                }
                "details" -> {
                    formattingStack.add(TokenType.Spoiler.style)
                }
                "code" -> {
                    insideCodeBlock = true
                    formattingStack.add(TokenType.CodeBlock.style)
                }
                "blockquote" -> {
                    insideQuote = true
                    formattingStack.add(TokenType.Quote.style)
                }
                "#text" -> {
                    if (node is TextNode && node.textBeforeChildren() != "") {
                        Main.serverLogger.info("Format text ${node.wholeText}")
                        val compToAdd =
                            node.textBeforeChildren().toComponent()
                        if (insideQuote) {
                            compToAdd.style(formattingStack.lastOrNull() ?: TokenType.Normal.style)
                            compToAdd.color(NamedTextColor.GREEN)
                        }
                        if (insideCodeBlock) {
                            compToAdd.style(TokenType.CodeBlock.style)
                        }
                        textComponent
                            .append(
                                compToAdd
                            )
                    }
                }
                else -> {
                }
            }
        }

        override fun tail(node: Node, depth: Int) {
            Main.serverLogger.info("Exiting node ${node.nodeName()}, ${node}")
            if (node is TextNode && node.textAfterChildren() != "" && node.textAfterChildren() != node.textBeforeChildren()) {
                Main.serverLogger.info("Format text ${node.wholeText}")
                val compToAdd =
                    node.textAfterChildren().toComponent()
                if (insideQuote) {
                    compToAdd.style(formattingStack.lastOrNull() ?: TokenType.Normal.style)
                    compToAdd.color(NamedTextColor.GREEN)
                }
                if (insideCodeBlock) {
                    compToAdd.style(TokenType.CodeBlock.style)
                }
                textComponent
                    .append(
                        compToAdd
                    )
                formattingStack.removeLastOrNull()
            }
            if (lastExitName == "code") {
                Main.serverLogger.info("No more code")
                insideCodeBlock = false
            }
            else if (lastExitName == "blockquote") {
                Main.serverLogger.info("No more quote")
                insideQuote = false
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

fun TextNode.textBeforeChildren(): String {
    val whole = this.wholeText
    val posOfFirstChild = whole.indexOfFirst {
        it == '<'
    }
    return if (posOfFirstChild == -1) {
        whole
    } else {
        whole.substring(0, posOfFirstChild)
    }
}

fun TextNode.textAfterChildren(): String {
    val whole = this.wholeText
    val posOfLastChild = whole.indexOfLast {
        it == '>'
    }
    return if (posOfLastChild == -1) {
        whole
    } else {
        whole.substring(posOfLastChild, whole.length-1)
    }
}