package dev.hermeskotlin.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

class MarkdownFenceTest {

    /** The fence the renderer is offered: the first code fence the text parses to. */
    private fun fence(markdown: String): FenceInfo {
        val root = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(markdown)
        val node = firstFence(root) ?: error("No code fence in: $markdown")
        return fenceInfo(markdown, node)
    }

    private fun firstFence(node: ASTNode): ASTNode? {
        if (node.type == MarkdownElementTypes.CODE_FENCE) return node
        for (child in node.children) firstFence(child)?.let { return it }
        return null
    }

    @Test
    fun aClosedUiFenceIsCompleteAndKeepsItsBody() {
        val fence = fence("```ui\n{\"type\":\"card\"}\n```")
        assertEquals("ui", fence.language)
        assertEquals("{\"type\":\"card\"}", fence.code)
        assertTrue(fence.complete)
    }

    @Test
    fun anUnclosedUiFenceIsIncompleteAndStillHasItsBody() {
        val fence = fence("```ui\n{\"type\":\"card\"}")
        assertEquals("ui", fence.language)
        assertEquals("{\"type\":\"card\"}", fence.code)
        assertFalse(fence.complete)
    }

    @Test
    fun anInfoStringKeepsOnlyItsFirstWord() {
        val fence = fence("```ui json\n{\"type\":\"card\"}\n```")
        assertEquals("ui", fence.language)
        assertEquals("{\"type\":\"card\"}", fence.code)
        assertTrue(fence.complete)
    }

    @Test
    fun aPlainFenceHasNoLanguage() {
        val fence = fence("```\nplain text\n```")
        assertNull(fence.language)
        assertEquals("plain text", fence.code)
        assertTrue(fence.complete)
    }
}
