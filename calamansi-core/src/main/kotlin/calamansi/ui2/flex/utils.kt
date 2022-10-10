package calamansi.ui2.flex

import calamansi.ui2.control.DimValue
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

internal fun applyStylePosition(ygNode: Long, value: DimValue, edge: Int) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetPosition(ygNode, edge, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetPositionPercent(ygNode, edge, value.pc)
        }

        DimValue.Undefined -> {
            // nada
        }
    }
}

internal fun applyStyleMargin(ygNode: Long, value: DimValue, edge: Int) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetMargin(ygNode, edge, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetMarginPercent(ygNode, edge, value.pc)
        }

        DimValue.Undefined -> {
            // nada
        }
    }
}

internal fun applyStylePadding(ygNode: Long, value: DimValue, edge: Int) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetPadding(ygNode, edge, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetPaddingPercent(ygNode, edge, value.pc)
        }

        DimValue.Undefined -> {
            // nada
        }
    }
}

internal fun applyStyleBasis(ygNode: Long, value: DimValue) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetFlexBasis(ygNode, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetFlexBasisPercent(ygNode, value.pc)
        }

        DimValue.Undefined -> {
            YGNodeStyleSetFlexBasisAuto(ygNode)
        }
    }
}

internal fun applyStyleWidth(ygNode: Long, value: DimValue) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetWidth(ygNode, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetWidthPercent(ygNode, value.pc)
        }

        DimValue.Undefined -> {
            YGNodeStyleSetWidthAuto(ygNode)
        }
    }
}

internal fun applyStyleMinWidth(ygNode: Long, value: DimValue) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetMinWidth(ygNode, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetMinWidthPercent(ygNode, value.pc)
        }

        DimValue.Undefined -> {}
    }
}

internal fun applyStyleMaxWidth(ygNode: Long, value: DimValue) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetMaxWidth(ygNode, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetMaxWidthPercent(ygNode, value.pc)
        }

        DimValue.Undefined -> {}
    }
}

internal fun applyStyleHeight(ygNode: Long, value: DimValue) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetHeight(ygNode, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetHeightPercent(ygNode, value.pc)
        }

        DimValue.Undefined -> {
            YGNodeStyleSetHeightAuto(ygNode)
        }
    }
}

internal fun applyStyleMinHeight(ygNode: Long, value: DimValue) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetMinHeight(ygNode, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetMinHeightPercent(ygNode, value.pc)
        }

        DimValue.Undefined -> {}
    }
}

internal fun applyStyleMaxHeight(ygNode: Long, value: DimValue) {
    when (value) {
        is DimValue.Fixed -> {
            YGNodeStyleSetMaxHeight(ygNode, value.value)
        }

        is DimValue.Percentage -> {
            YGNodeStyleSetMaxHeightPercent(ygNode, value.pc)
        }

        DimValue.Undefined -> {}
    }
}