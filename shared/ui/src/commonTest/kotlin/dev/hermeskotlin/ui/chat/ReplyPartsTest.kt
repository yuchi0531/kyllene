package dev.hermeskotlin.ui.chat

import com.composables.icons.lucide.FileDiff
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SquareTerminal
import com.composables.icons.lucide.Wrench
import dev.hermeskotlin.core.chat.ToolActivity
import dev.hermeskotlin.ui.resources.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplyPartsTest {

    @Test
    fun theToolPillAddsUpTheTimedCalls() {
        val tools = listOf(
            ToolActivity("1", "terminal", durationSeconds = 30.4),
            ToolActivity("2", "read_file", durationSeconds = 12.0),
            ToolActivity("3", "patch"),
        )

        assertEquals(ran(3) to "42s", workedLabel(tools))
    }

    private fun ran(count: Int) = Words.Count(Res.plurals.chat_tools_ran, count)

    @Test
    fun withNoTimesThePillSaysOnlyHowManyRan() {
        assertEquals(ran(1) to null, workedLabel(listOf(ToolActivity("1", "terminal"))))
    }

    @Test
    fun aMinuteOrMoreIsInMinutes() {
        assertEquals(ran(2) to "1m 15s", workedLabel(listOf(ToolActivity("1", "a", durationSeconds = 60.0), ToolActivity("2", "b", durationSeconds = 15.0))))
    }

    @Test
    fun toolsGetTheIconOfTheirKind() {
        assertEquals(Lucide.SquareTerminal, toolIcon("terminal"))
        assertEquals(Lucide.FileDiff, toolIcon("patch"))
        assertEquals(Lucide.Globe, toolIcon("browser_navigate"))
        assertEquals(Lucide.Wrench, toolIcon("some_plugin_tool"))
    }

    @Test
    fun usageReadsInThenOut() {
        assertEquals("18.4k ↓ · 612 ↑", replyUsage(18_400, 612))
    }

    @Test
    fun usageAddsItsSpeedAndLengthWhenTheTurnWasWatched() {
        // 612 tokens written over 3.5 s, in a turn of 4 s with the wait before the first one.
        assertEquals("18.4k ↓ · 612 ↑ · 175 tok/s · 4s", replyUsage(18_400, 612, 3.5, 4.0))
        // A reply no device watched says only what it read and wrote.
        assertEquals("18.4k ↓ · 612 ↑", replyUsage(18_400, 612, null, null))
        // A turn too quick to time says the same.
        assertEquals("18.4k ↓ · 612 ↑", replyUsage(18_400, 612, 0.0, 0.0))
        // Under a hundred a second keeps its tenth.
        assertEquals("18.4k ↓ · 612 ↑ · 42.2 tok/s · 14.5s", replyUsage(18_400, 612, 14.5, 14.5))
    }
}
