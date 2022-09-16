package calamansi.ui

import calamansi.meta.Property

open class Text : CanvasElement() {
    @Property
    var text: String = ""

    @Property
    var fontSize: Float = 10.0f
}