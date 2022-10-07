package calamansi.ui

import calamansi.bus.Message
import calamansi.event.Event
import calamansi.gfx.Color
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.KeyStateEvent
import calamansi.input.TextEvent
import calamansi.meta.Property
import calamansi.node.ExecutionContext
import io.github.humbleui.skija.Canvas
import kotlin.math.min

class TextArea(@Property override var text: String = "") : TextBase() {
    private var cursorPos = text.length
    private val buffer = mutableListOf<Char>()

    init {
        text.toCollection(buffer)
        subscribe {
            handleMessage(it)
        }
    }

    context(ExecutionContext) override fun onEvent(event: Event) {
        super.onEvent(event)

        when (event) {
            is TextEvent -> {
                // text += event.char
                buffer.add(cursorPos++, event.char)
                event.consume()
                recomputeText()
            }

            is KeyStateEvent -> {
                if (event.key == Key.BACKSPACE && event.state == InputState.RELEASED) {
                    if (cursorPos > 0) {
                        buffer.removeAt(--cursorPos)
                    }
                    event.consume()
                    recomputeText()
                }
            }
        }
    }

    context(ExecutionContext) private fun handleMessage(message: Message) {
        when (message) {
            is CanvasMessage.ElementExit -> {
                setCursor(Cursor.HAND)
            }

            is CanvasMessage.ElementEnter -> {
                setCursor(Cursor.IBEAM)
            }
        }
    }

    context(ExecutionContext) private fun recomputeText() {
        text = buffer.joinToString(separator = "")
        publish(CanvasMessage.TextInputChange(this, text))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        // draw cursor
        var cursorX = 0f
        var cursorY = 0f
        val metrics = skijaFont.metrics
        if (text.isNotEmpty()) {
            // spaces on linebreaks can be collapsed into a single space, when
            // this happens text.size won't match glyphs.size and glyphPositions.size
            // since we use glyph positions to compute where the cursor is drawn
            // TODO: when using makeShaperDrivenWrapper() this won't happen, but we should stop
            //  drawing the cursor when it goes out of bounds.
            val cursorPos = min(cursorPos, glyphs.size)
            val index = (cursorPos - 1) * 2
            cursorX = glyphPositions[index] + glyphWidths[cursorPos - 1]
            cursorY =
                glyphPositions[index + 1] - fontSize /* height of each line is approximately the size of the font */
        }
        cursorX += getPaddingLeft() + getBorderLeft()
        cursorY += getPaddingTop() + getBorderTop()
        canvas.drawLine(cursorX, cursorY, cursorX, cursorY + metrics.height, Color.BLACK.toPaint().setStrokeWidth(1.5f))
    }
}