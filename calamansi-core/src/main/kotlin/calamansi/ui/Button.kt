package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property
import calamansi.node.ExecutionContext

class Button(text: String = "") : TextBase(text) {
    @Property
    var highlightColor: Color = Color.CYAN

    @Property
    var pressedColor: Color = Color.MAGENTA

    override var backgroundColor: Color = Color.BLUE

    override fun getBackgroundColor(): Color {
        return if (isPressed()) {
            pressedColor
        } else if (isHovered()) {
            highlightColor
        } else {
            backgroundColor
        }
    }

    context(ExecutionContext) override fun onMouseEnter() {
        setCursor(Cursor.HAND)
    }

    context(ExecutionContext) override fun onMouseExit() {
        setCursor(Cursor.ARROW)
    }
}