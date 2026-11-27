package dev.hermeskotlin.ui.chat

import android.content.ClipData
import android.net.Uri
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ClipboardImagesTest {
    @Test
    fun picksOnlyThePicturesAmongTheClipboardUris() {
        val picture = Uri.parse("content://photos/1")
        val duplicate = Uri.parse("content://photos/1")
        val document = Uri.parse("content://files/report.pdf")
        val clip = ClipData.newRawUri("", picture).apply {
            addItem(ClipData.Item(document))
            addItem(ClipData.Item(duplicate))
            addItem(ClipData.Item("not a URI"))
        }
        assertEquals(listOf(picture), clip.imageUris { it == picture || it == duplicate })
    }

    @Test
    fun aTextOnlyClipboardHasNoPictures() {
        val clip = ClipData.newPlainText("", "hello")
        assertEquals(emptyList(), clip.imageUris { true })
    }
}
