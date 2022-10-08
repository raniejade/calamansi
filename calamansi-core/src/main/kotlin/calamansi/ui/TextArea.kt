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
import io.github.humbleui.skija.paragraph.RectHeightMode
import io.github.humbleui.skija.paragraph.RectWidthMode

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
        if (text.isNotEmpty()) {
            val rect = pg.getRectsForRange(cursorPos - 1, cursorPos, RectHeightMode.TIGHT, RectWidthMode.TIGHT)[0]
            cursorX += rect.rect.right
            cursorY += rect.rect.top

            if (cursorX > getAvailableWidth()) {
                cursorX = getAvailableWidth()
            }
        }
        cursorX += getPaddingLeft() + getBorderLeft()
        cursorY += getPaddingTop() + getBorderTop()
        canvas.drawLine(cursorX, cursorY, cursorX, cursorY + fontSize, Color.BLACK.toPaint().setStrokeWidth(1.5f))
    }
}