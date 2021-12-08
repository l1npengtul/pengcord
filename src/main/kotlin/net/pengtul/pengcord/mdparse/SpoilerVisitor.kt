package net.pengtul.pengcord.mdparse

interface SpoilerVisitor {
    fun visit(node: Spoiler)
}