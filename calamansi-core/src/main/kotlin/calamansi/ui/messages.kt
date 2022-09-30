package calamansi.ui

import calamansi.bus.Message
import calamansi.input.MouseButton

sealed class CanvasMessage(val source: CanvasElement) : Message {
    class ElementEnter(source: CanvasElement) : CanvasMessage(source)
    class ElementExit(source: CanvasElement) : CanvasMessage(source)
    class ElementMousePress(source: CanvasElement, val button: MouseButton) : CanvasMessage(source)
    class ElementMouseRelease(source: CanvasElement, val button: MouseButton) : CanvasMessage(source)
    class ButtonPress(source: CanvasElement) : CanvasMessage(source)
}