package dev.hermeskotlin.ui.chat

import dev.hermeskotlin.core.chat.GenUi
import dev.hermeskotlin.core.chat.GenUiAction
import dev.hermeskotlin.core.chat.GenUiNode
import dev.hermeskotlin.core.chat.GenUiParseResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenUiCardTest {

    private fun parsed(code: String): GenUiNode {
        val result = GenUi.parse(code)
        assertTrue(result is GenUiParseResult.Ok, "expected a parsed card, got $result")
        return result.root
    }

    @Test
    fun aSendCarriesTheEventContextAndFormValues() {
        val node = parsed(
            """{"type":"card","children":[{"type":"button","id":"save","event":"save",
               "context":{"day":"sat"},"action":{"type":"send"},"text":"Save"}]}""",
        )
        val button = node.children.single()
        val text = genUiSendText(GenUiAction.Send(null), button, mapOf("budget" to "50"))
        assertEquals(
            """Tapped “save” on the card you showed: {"context":{"day":"sat"},"data":{"budget":"50"}}""",
            text,
        )
    }

    @Test
    fun aSendWithOnlyTextSendsThatText() {
        val node = parsed("""{"type":"button","id":"say","action":{"type":"send"},"text":"Just this"}""")
        assertEquals(
            "Just this",
            genUiSendText(GenUiAction.Send("Just this"), node, emptyMap()),
        )
    }

    @Test
    fun anEventWithNoExtrasStaysOneShortLine() {
        val node = parsed("""{"type":"button","id":"pick","event":"pick-b"}""")
        assertEquals(
            "Tapped “pick-b” on the card you showed.",
            genUiSendText(GenUiAction.Send(null), node, emptyMap()),
        )
    }

    @Test
    fun componentAliasesFoldToTheOnesTheRendererKnows() {
        assertEquals("stack", genUiType("Column"))
        assertEquals("row", genUiType("hstack"))
        assertEquals("card", genUiType("card"))
        assertEquals("metric", genUiType("kpi"))
        assertEquals("code", genUiType("codeblock"))
        assertEquals("table", genUiType("TABLE"))
    }

    @Test
    fun tonesFoldToTheDesignSystemsStatuses() {
        assertEquals("good", genUiTone("success"))
        assertEquals("warn", genUiTone("warning"))
        assertEquals("bad", genUiTone("error"))
        assertEquals("info", genUiTone("accent"))
        assertEquals("neutral", genUiTone(null))
    }
}
