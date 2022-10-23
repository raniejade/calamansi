package calamansi.ui2.control

import calamansi.ui2.control.FlexAlign
import calamansi.ui2.control.FlexDirection
import calamansi.ui2.control.FlexJustify
import calamansi.ui2.control.FlexWrap
import org.lwjgl.util.yoga.Yoga.*

internal fun FlexAlign.toYGValue(): Int {
    return when (this) {
        FlexAlign.AUTO -> YGAlignAuto
        FlexAlign.FLEX_START -> YGAlignFlexStart
        FlexAlign.FLEX_END -> YGAlignFlexEnd
        FlexAlign.STRETCH -> YGAlignStretch
        FlexAlign.CENTER -> YGAlignCenter
        FlexAlign.SPACE_BETWEEN -> YGAlignSpaceBetween
        FlexAlign.SPACE_AROUND -> YGAlignSpaceAround
        FlexAlign.BASELINE -> YGAlignBaseline
    }
}

internal fun FlexDirection.toYGValue(): Int {
    return when (this) {
        FlexDirection.ROW -> YGFlexDirectionRow
        FlexDirection.COLUMN -> YGFlexDirectionColumn
        FlexDirection.ROW_REVERSE -> YGFlexDirectionRowReverse
        FlexDirection.COLUMN_REVERSE -> YGFlexDirectionColumnReverse
    }
}

internal fun FlexJustify.toYGValue(): Int {
    return when (this) {
        FlexJustify.FLEX_START -> YGJustifyFlexStart
        FlexJustify.FLEX_END -> YGJustifyFlexEnd
        FlexJustify.CENTER -> YGJustifyCenter
        FlexJustify.SPACE_BETWEEN -> YGJustifySpaceBetween
        FlexJustify.SPACE_AROUND -> YGJustifySpaceAround
        FlexJustify.SPACE_EVENLY -> YGJustifySpaceEvenly
    }
}

internal fun FlexWrap.toYGValue(): Int {
    return when (this) {
        FlexWrap.NO_WRAP -> YGWrapNoWrap
        FlexWrap.WRAP -> YGWrapWrap
        FlexWrap.WRAP_REVERSE -> YGWrapReverse
    }
}