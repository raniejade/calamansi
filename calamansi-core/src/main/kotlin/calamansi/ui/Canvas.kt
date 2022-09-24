package calamansi.ui

import calamansi.gfx.Color
import org.jetbrains.skija.Paint

class Canvas : FlexElement {
    override var alignContent: FlexAlign = FlexAlign.FLEX_START

    override var alignItems: FlexAlign = FlexAlign.STRETCH

    override var direction: FlexDirection = FlexDirection.ROW

    override var justifyContent: FlexJustify = FlexJustify.FLEX_START

    override var wrap: FlexWrap = FlexWrap.WRAP

    override var layout: FlexLayout = FlexLayout.RELATIVE

    override var position: FlexBounds = FlexBounds()

    override var margin: FlexBounds = FlexBounds()

    override var padding: FlexBounds = FlexBounds()

    override var alignSelf: FlexAlign = FlexAlign.AUTO

    override var grow: Float = 0f

    override var shrink: Float = 1f

    override var basis: FlexValue? = null

    override var width: FlexValue? = null

    override var height: FlexValue? = null

    override var minWidth: FlexValue? = null

    override var minHeight: FlexValue? = null

    override var maxWidth: FlexValue? = null

    override var maxHeight: FlexValue? = null

    override var backgroundColor: Color = Color.TRANSPARENT
        set(value) {
            field = value
            backgroundPaint.close()
            backgroundPaint = value.toPaint()
        }

    internal var backgroundPaint: Paint = backgroundColor.toPaint()
}