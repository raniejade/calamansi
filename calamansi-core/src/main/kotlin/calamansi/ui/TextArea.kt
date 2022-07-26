package calamansi.ui

import calamansi.bus.Message
import calamansi.gfx.Color
import calamansi.input.*
import calamansi.meta.Property
import calamansi.node.ExecutionContext
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.paragraph.RectHeightMode
import io.github.humbleui.skija.paragraph.RectWidthMode

class TextArea(@Property override var text: String = "") : TextBase() {
    private var cursorPos = text.length
    private val buffer = mutableListOf<Char>()
    private val tickInterval = 1000f / 10
    private var elapsedTime = 0f
    private var drawCursor = true

    init {
        text.toCollection(buffer)
        subscribe {
            handleMessage(it)
        }
    }

    context(ExecutionContext) override fun onGuiEvent(event: InputEvent) {
        super.onGuiEvent(event)

        // TODO: move to WindowContext?
        if (!isFocused()) {
            return
        }

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

            else -> {}
        }
    }

    context(ExecutionContext) private fun handleMessage(message: Message) {
        when (message) {
            is CanvasMessage.ElementExit -> {
                setCursor(Cursor.ARROW)
            }

            is CanvasMessage.ElementEnter -> {
                setCursor(Cursor.IBEAM)
            }

            is CanvasMessage.ElementFocus -> {
                resetCursorTick()
            }
        }
    }

    context(ExecutionContext) private fun recomputeText() {
        text = buffer.joinToString(separator = "")
        publish(CanvasMessage.TextInputChange(this, text))
        resetCursorTick()
    }

    override fun isFocusable(): Boolean {
        return true
    }

    context(ExecutionContext) override fun onUpdate(delta: Float) {
        super.onUpdate(delta)
        if (isFocused()) {
            cursorTick(delta)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (isFocused() && drawCursor) {
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

    private fun cursorTick(delta: Float) {
        elapsedTime += delta
        if (elapsedTime > tickInterval) {
            elapsedTime = 0f
            drawCursor = !drawCursor
        }
    }

    private fun resetCursorTick() {
        drawCursor = true
        elapsedTime = 0f
    }
}