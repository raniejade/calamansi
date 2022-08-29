package calamansi.ui

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

data class CornerValue(var top: Float = 0f, var left: Float = 0f, var bottom: Float = 0f, var right: Float = 0f)

sealed class AxisValue {
    object Auto : AxisValue()
    class Fixed(val value: Float) : AxisValue()
    class Relative(val percentage: Float) : AxisValue() {
        init {
            require(percentage.toInt() in 0..100)
        }
    }
}

interface Element {
    var layout: FlexLayout
    var position: CornerValue
    var margin: CornerValue
    var padding: CornerValue
    var alignSelf: FlexAlign
    var grow: Float
    var shrink: Float
    var basis: AxisValue
    var width: AxisValue
    var height: AxisValue
    var minWidth: AxisValue
    var minHeight: AxisValue
    var maxWidth: AxisValue
    var maxHeight: AxisValue
}

interface Container : Element {
    var alignContent: FlexAlign
    var alignItems: FlexAlign
    var direction: FlexDirection
    var justifyContent: FlexJustify
    var wrap: FlexWrap
}