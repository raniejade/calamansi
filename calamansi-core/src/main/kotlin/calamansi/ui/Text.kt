package calamansi.ui

import calamansi.runtime.WindowContext
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skija.TextBlob
import org.jetbrains.skija.shaper.Shaper
import java.util.*

open class Text(text: String = "") : CanvasElement() {
    private var stateHash: Int = 0
    private lateinit var blob: TextBlob
    var text: String = text
    var size: Float = 12f

    override fun applyLayout() {
        super.applyLayout()
        val font = (executionContext as WindowContext).defaultFont.get()
        val newStateHash = Objects.hash(text, size, width, maxWidth, font)
        if (newStateHash != stateHash) {
            // cleanup
            if (::blob.isInitialized) {
                blob.close()
            }

            blob = Shaper.make().use { shaper ->
                val width = width
                if (width is FlexValue.Fixed) {
                    shaper.shape(text, font.makeSkijaFont(size), width.value)
                } else {
                    shaper.shape(text, font.makeSkijaFont(size))
                }!!
            }
            stateHash = newStateHash
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        Paint().setARGB(255, 128, 232, 162).use {
            canvas.drawTextBlob(blob, 0f, 0f, it)
        }
    }
}