package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property

class Button(text: String = "") : TextBase(text) {
    @Property
    var highlightColor: Color = Color.CYAN

    @Property
    var pressedColor: Color = Color.CYAN

    override var backgroundColor: Color = Color.BLUE

    override fun getBackgroundColor(): Color {
        return if (isHovered()) {
            highlightColor
        } else if (isPressed()) {
            pressedColor
        } else {
            backgroundColor
        }
    }
}