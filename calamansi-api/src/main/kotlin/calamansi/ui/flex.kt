package calamansi.ui

import calamansi.meta.Property
import calamansi.node.Node
import kotlinx.serialization.Serializable

enum class FlexLayout {
    RELATIVE,
    ABSOLUTE,
}

enum class FlexAlign {
    AUTO,
    FLEX_START,
    FLEX_END,
    STRETCH,
    CENTER,
    SPACE_BETWEEN,
    SPACE_AROUND,
    BASELINE,
}

enum class FlexDirection {
    ROW,
    COLUMN,
    ROW_REVERSE,
    COLUMN_REVERSE,
}

enum class FlexJustify {
    FLEX_START,
    FLEX_END,
    CENTER,
    SPACE_BETWEEN,
    SPACE_AROUND,
    SPACE_EVENLY,
}

enum class FlexWrap {
    NO_WRAP,
    WRAP,
    WRAP_REVERSE,
}

@Serializable
data class FlexBounds(var top: Float = 0f, var left: Float = 0f, var bottom: Float = 0f, var right: Float = 0f)

@Serializable
sealed class FlexAxisValue {
    @Serializable
    object Auto : FlexAxisValue()

    @Serializable
    class Fixed(val value: Float) : FlexAxisValue()

    @Serializable
    class Relative(val percentage: Float) : FlexAxisValue() {
        init {
            require(percentage.toInt() in 0..100)
        }
    }
}

open class CanvasElement : Node() {
    @Property
    var alignContent: FlexAlign = FlexAlign.FLEX_START

    @Property
    var alignItems: FlexAlign = FlexAlign.STRETCH

    @Property
    var direction: FlexDirection = FlexDirection.ROW

    @Property
    var justifyContent: FlexJustify = FlexJustify.FLEX_START

    @Property
    var wrap: FlexWrap = FlexWrap.WRAP

    @Property
    var layout: FlexLayout = FlexLayout.RELATIVE

    @Property
    var position: FlexBounds = FlexBounds()

    @Property
    var margin: FlexBounds = FlexBounds()

    @Property
    var padding: FlexBounds = FlexBounds()

    @Property
    var alignSelf: FlexAlign = FlexAlign.AUTO

    @Property
    var grow: Float = 0f

    @Property
    var shrink: Float = 1f

    @Property
    var basis: FlexAxisValue = FlexAxisValue.Auto

    @Property
    var width: FlexAxisValue = FlexAxisValue.Auto

    @Property
    var height: FlexAxisValue = FlexAxisValue.Auto

    @Property
    var minWidth: FlexAxisValue = FlexAxisValue.Auto

    @Property
    var minHeight: FlexAxisValue = FlexAxisValue.Auto

    @Property
    var maxWidth: FlexAxisValue = FlexAxisValue.Auto

    @Property
    var maxHeight: FlexAxisValue = FlexAxisValue.Auto
}