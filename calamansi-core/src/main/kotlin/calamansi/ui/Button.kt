package calamansi.ui

import calamansi.node.ExecutionContext

class Button(text: String = "") : TextBase(text) {
    context(ExecutionContext) override fun onMouseEnter() {
        setCursor(Cursor.HAND)
    }

    context(ExecutionContext) override fun onMouseExit() {
        setCursor(Cursor.ARROW)
    }
}