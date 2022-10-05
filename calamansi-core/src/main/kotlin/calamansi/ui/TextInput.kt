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

class TextInput(@Property override var text: String = "") : TextBase() {
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
        val blob = blob
        if (blob != null) {
            val positions = blob.positions
            val index = (cursorPos - 1) * 2
            cursorX = positions[index] + skijaFont.getWidths(shortArrayOf(blob.glyphs[cursorPos - 1]))[0]
            cursorY = positions[index + 1] - fontSize
        }
        cursorX += getLayoutLeft() + getPaddingLeft() + getBorderLeft()
        cursorY += getLayoutTop() + getPaddingTop() + getBorderTop()
        canvas.drawLine(cursorX, cursorY, cursorX, cursorY + metrics.height, Color.BLACK.toPaint().setStrokeWidth(1.5f))
    }
}