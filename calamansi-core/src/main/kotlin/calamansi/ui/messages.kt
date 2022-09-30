package calamansi.ui

import calamansi.bus.Message
import calamansi.input.MouseButton

sealed class CanvasMessage<T : CanvasElement>(val source: T) : Message {
    class ElementEnter(source: CanvasElement) : CanvasMessage<CanvasElement>(source)
    class ElementExit(source: CanvasElement) : CanvasMessage<CanvasElement>(source)
    class ElementMousePress(source: CanvasElement, val button: MouseButton) : CanvasMessage<CanvasElement>(source)
    class ElementMouseRelease(source: CanvasElement, val button: MouseButton) : CanvasMessage<CanvasElement>(source)
    class ButtonPress(source: Button) : CanvasMessage<Button>(source)
}