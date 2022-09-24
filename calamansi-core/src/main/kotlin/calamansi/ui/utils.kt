package calamansi.ui

import calamansi.gfx.Color
import org.jetbrains.skija.Paint
import org.lwjgl.util.yoga.Yoga.*

internal fun Color.toPaint(): Paint {
    return Paint().setARGB(a, r, g, b)
}

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

internal fun FlexElement.applyStyle(ygNode: Long) {
    YGNodeStyleSetFlexDirection(ygNode, direction.toYGValue())
    YGNodeStyleSetAlignItems(ygNode, alignItems.toYGValue())
    YGNodeStyleSetAlignContent(ygNode, alignContent.toYGValue())
    YGNodeStyleSetJustifyContent(ygNode, justifyContent.toYGValue())

    YGNodeStyleSetPositionType(
        ygNode,
        when (layout) {
            FlexLayout.RELATIVE -> YGPositionTypeRelative
            FlexLayout.ABSOLUTE -> YGPositionTypeAbsolute
        }
    )

    YGNodeStyleSetAlignSelf(ygNode, alignSelf.toYGValue())

    applyStylePosition(ygNode, position.left, YGEdgeLeft)
    applyStylePosition(ygNode, position.top, YGEdgeTop)
    applyStylePosition(ygNode, position.bottom, YGEdgeBottom)
    applyStylePosition(ygNode, position.right, YGEdgeRight)

    applyStyleMargin(ygNode, margin.left, YGEdgeLeft)
    applyStyleMargin(ygNode, margin.top, YGEdgeTop)
    applyStyleMargin(ygNode, margin.bottom, YGEdgeBottom)
    applyStyleMargin(ygNode, margin.right, YGEdgeRight)

    applyStylePadding(ygNode, padding.left, YGEdgeLeft)
    applyStylePadding(ygNode, padding.top, YGEdgeTop)
    applyStylePadding(ygNode, padding.bottom, YGEdgeBottom)
    applyStylePadding(ygNode, padding.right, YGEdgeRight)

}

private fun applyStylePosition(ygNode: Long, value: FlexValue, edge: Int) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetPosition(ygNode, edge, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetPositionPercent(ygNode, edge, value.pc)
        }
    }
}

private fun applyStyleMargin(ygNode: Long, value: FlexValue, edge: Int) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetMargin(ygNode, edge, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetMarginPercent(ygNode, edge, value.pc)
        }
    }
}

private fun applyStylePadding(ygNode: Long, value: FlexValue, edge: Int) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetPadding(ygNode, edge, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetPaddingPercent(ygNode, edge, value.pc)
        }
    }
}