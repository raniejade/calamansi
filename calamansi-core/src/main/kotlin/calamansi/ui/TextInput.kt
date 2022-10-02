package calamansi.ui

import calamansi.bus.Message
import calamansi.node.ExecutionContext

class TextInput(text: String = "") : TextBase(text) {
    init {
        subscribe {
            handleMessage(it)
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