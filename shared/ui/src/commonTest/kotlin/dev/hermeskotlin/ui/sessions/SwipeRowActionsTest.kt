package dev.hermeskotlin.ui.sessions

import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pin
import com.composables.icons.lucide.Trash2
import kotlin.test.Test
import kotlin.test.assertEquals

class SwipeRowActionsTest {

    @Test
    fun oneActionOnBothSidesIsOfferedToScreenReadersOnce() {
        val read = SwipeAction("Mark read", Lucide.Check, SwipeKind.Pin) {}
        assertEquals(listOf("Mark read"), readerActions(read, read).map { it.label })
    }

    @Test
    fun aSessionRowsTwoActionsAreBothOffered() {
        val pin = SwipeAction("Pin", Lucide.Pin, SwipeKind.Pin) {}
        val delete = SwipeAction("Delete", Lucide.Trash2, SwipeKind.Delete) {}
        assertEquals(listOf("Pin", "Delete"), readerActions(pin, delete).map { it.label })
        assertEquals(listOf("Delete"), readerActions(null, delete).map { it.label })
        assertEquals(emptyList(), readerActions(null, null))
    }
}
