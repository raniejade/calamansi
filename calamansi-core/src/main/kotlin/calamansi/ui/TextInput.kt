package calamansi.ui

import calamansi.bus.Message
import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.KeyStateEvent
import calamansi.input.TextEvent
import calamansi.node.ExecutionContext

class TextInput(text: String = "") : TextBase(text) {
    init {
        subscribe {
            handleMessage(it)
        }
    }

    private var cursorPos = 0

    context(ExecutionContext) override fun onEvent(event: Event) {
        super.onEvent(event)

        when (event) {
            is TextEvent -> {
                text += event.char
                event.consume()
            }
            is KeyStateEvent -> {
                if (event.key == Key.BACKSPACE && event.state == InputState.RELEASED) {
                    text = text.substring(0, text.length - 1)
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
}