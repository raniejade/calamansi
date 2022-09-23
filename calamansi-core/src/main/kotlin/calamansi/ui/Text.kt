package calamansi.ui

import org.jetbrains.skija.TextBlob
import org.jetbrains.skija.shaper.Shaper
import java.util.*

open class Text : CanvasElement() {
    private var stateHash: Int? = null
    private lateinit var blob: TextBlob
    var text: String = ""
    var size: Float = 12f

    override fun applyLayout() {
        super.applyLayout()
        val font = computeFont()
        val newStateHash = Objects.hash(text, size, width, maxWidth, font)
        if (stateHash != newStateHash) {
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
}