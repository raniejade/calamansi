package calamansi.ui

import org.lwjgl.util.yoga.Yoga
import org.lwjgl.util.yoga.Yoga.*

fun FlexAlign.toYGValue(): Int {
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

fun FlexDirection.toYGValue(): Int {
    return when (this) {
        FlexDirection.ROW -> YGFlexDirectionRow
        FlexDirection.COLUMN -> YGFlexDirectionColumn
        FlexDirection.ROW_REVERSE -> YGFlexDirectionRowReverse
        FlexDirection.COLUMN_REVERSE -> YGFlexDirectionColumnReverse
    }
}

fun FlexJustify.toYGValue(): Int {
    return when (this) {
        FlexJustify.FLEX_START -> YGJustifyFlexStart
        FlexJustify.FLEX_END -> YGJustifyFlexEnd
        FlexJustify.CENTER -> YGJustifyCenter
        FlexJustify.SPACE_BETWEEN -> YGJustifySpaceBetween
        FlexJustify.SPACE_AROUND -> YGJustifySpaceAround
        FlexJustify.SPACE_EVENLY -> YGJustifySpaceEvenly
    }
}