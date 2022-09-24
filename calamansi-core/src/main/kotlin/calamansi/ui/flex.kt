package calamansi.ui

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
sealed interface FlexValue {
    @Serializable
    class Fixed(val value: Float) : FlexValue

    @Serializable
    class Relative(val pc: Float) : FlexValue {
        init {
            require(pc.toInt() in 0..100)
        }
    }
}

@Serializable
data class FlexBounds(
    var top: FlexValue = FlexValue.Fixed(0f),
    var left: FlexValue = FlexValue.Fixed(0f),
    var bottom: FlexValue = FlexValue.Fixed(0f),
    var right: FlexValue = FlexValue.Fixed(0f)
)
