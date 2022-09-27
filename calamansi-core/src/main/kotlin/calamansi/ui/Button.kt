package calamansi.ui

import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.MouseButton
import calamansi.input.MouseButtonStateEvent
import calamansi.meta.Property
import calamansi.node.ExecutionContext

class Button(text: String = "") : TextBase(text) {
    @Property
    var pressedStyledBox: StyledBox = EmptyStyledBox()

    private var _pressed = false

    override val currentStyleBox: StyledBox
        get() {
            return if (isPressed()) {
                pressedStyledBox
            } else {
                super.currentStyleBox
            }
        }

    fun isPressed() = _pressed

    override fun onThemeChanged(theme: Theme) {
        super.onThemeChanged(theme)
        pressedStyledBox = theme.getStyledBox(this::class, "pressed")
    }

    context(ExecutionContext) override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is MouseButtonStateEvent -> {
                if (event.state == InputState.PRESSED && isHovered()) {
                    _pressed = true
                } else if (event.state == InputState.RELEASED && isPressed()) {
                    if (isHovered()) {
                        onMousePressed(event.button)
                    }
                    _pressed = false
                }
            }
        }
    }

    context (ExecutionContext) protected open fun onMousePressed(button: MouseButton) = Unit

    context(ExecutionContext) override fun onMouseEnter() {
        setCursor(Cursor.HAND)
    }

    context(ExecutionContext) override fun onMouseExit() {
        setCursor(Cursor.ARROW)
    }
}