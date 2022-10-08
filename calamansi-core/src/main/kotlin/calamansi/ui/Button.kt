package calamansi.ui

import calamansi.bus.Message
import calamansi.input.MouseButton
import calamansi.meta.Property
import calamansi.node.ExecutionContext
import calamansi.runtime.WindowContext

class Button(@Property override var text: String = "") : TextBase() {
    @Property
    var pressedStyledBox: StyledBox by Theme.styledBox()

    private var _pressed = false

    init {
        subscribe {
            handleMessage(it)
        }
    }

    override val currentStyleBox: StyledBox
        get() {
            return if (isPressed()) {
                pressedStyledBox
            } else {
                super.currentStyleBox
            }
        }

    fun isPressed() = _pressed

    override fun nodeExitTree() {
        // when a button press causes a scene change, reset cursor
        if (_pressed) {
            (executionContext as WindowContext).setCursor(Cursor.ARROW)
            _pressed = false
        }
    }

    context(ExecutionContext) private fun handleMessage(message: Message) {
        when (message) {
            is CanvasMessage.ElementExit -> {
                setCursor(Cursor.ARROW)

                if (_pressed) {
                    _pressed = false
                }
            }

            is CanvasMessage.ElementEnter -> {
                setCursor(Cursor.HAND)
            }

            is CanvasMessage.ElementMousePress -> {
                if (message.button == MouseButton.BUTTON_1) {
                    _pressed = true
                }
            }

            is CanvasMessage.ElementMouseRelease -> {
                if (message.button == MouseButton.BUTTON_1) {
                    if (isHovered()) {
                        publish(CanvasMessage.ButtonPress(this))
                    }
                    _pressed = false
                }
            }
        }
    }
}