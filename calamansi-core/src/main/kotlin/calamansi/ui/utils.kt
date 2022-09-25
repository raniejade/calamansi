package calamansi.ui

import calamansi.gfx.Color
import org.jetbrains.skija.Paint
import org.lwjgl.util.yoga.Yoga.*

// TODO: cache me
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

internal fun FlexWrap.toYGValue(): Int {
    return when (this) {
        FlexWrap.NO_WRAP -> YGWrapNoWrap
        FlexWrap.WRAP -> YGWrapWrap
        FlexWrap.WRAP_REVERSE -> YGWrapReverse
    }
}

internal fun FlexElement.applyStyle(ygNode: Long) {
    YGNodeStyleSetFlexDirection(ygNode, direction.toYGValue())
    YGNodeStyleSetAlignItems(ygNode, alignItems.toYGValue())
    YGNodeStyleSetAlignContent(ygNode, alignContent.toYGValue())
    YGNodeStyleSetJustifyContent(ygNode, justifyContent.toYGValue())
    YGNodeStyleSetAlignSelf(ygNode, alignSelf.toYGValue())

    YGNodeStyleSetPositionType(
        ygNode,
        when (layout) {
            FlexLayout.RELATIVE -> YGPositionTypeRelative
            FlexLayout.ABSOLUTE -> YGPositionTypeAbsolute
        }
    )

    YGNodeStyleSetFlexWrap(ygNode, wrap.toYGValue())

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

    applyStyleWidth(ygNode, width)
    applyStyleMinWidth(ygNode, minWidth)
    applyStyleMaxWidth(ygNode, maxWidth)
    applyStyleHeight(ygNode, height)
    applyStyleMinHeight(ygNode, minHeight)
    applyStyleMaxHeight(ygNode, maxHeight)

    applyStyleBasis(ygNode, basis)
    YGNodeStyleSetFlexGrow(ygNode, grow)
    YGNodeStyleSetFlexShrink(ygNode, shrink)

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

private fun applyStyleBasis(ygNode: Long, value: FlexValue?) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetFlexBasis(ygNode, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetFlexBasisPercent(ygNode, value.pc)
        }

        null -> {
            YGNodeStyleSetFlexBasisAuto(ygNode)
        }
    }
}

private fun applyStyleWidth(ygNode: Long, value: FlexValue?) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetWidth(ygNode, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetWidthPercent(ygNode, value.pc)
        }

        null -> {
            YGNodeStyleSetWidthAuto(ygNode)
        }
    }
}

private fun applyStyleMinWidth(ygNode: Long, value: FlexValue?) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetMinWidth(ygNode, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetMinWidthPercent(ygNode, value.pc)
        }

        null -> {}
    }
}

private fun applyStyleMaxWidth(ygNode: Long, value: FlexValue?) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetMaxWidth(ygNode, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetMaxWidthPercent(ygNode, value.pc)
        }

        null -> {}
    }
}

private fun applyStyleHeight(ygNode: Long, value: FlexValue?) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetHeight(ygNode, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetHeightPercent(ygNode, value.pc)
        }

        null -> {
            YGNodeStyleSetHeightAuto(ygNode)
        }
    }
}

private fun applyStyleMinHeight(ygNode: Long, value: FlexValue?) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetMinHeight(ygNode, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetMinHeightPercent(ygNode, value.pc)
        }

        null -> {}
    }
}

private fun applyStyleMaxHeight(ygNode: Long, value: FlexValue?) {
    when (value) {
        is FlexValue.Fixed -> {
            YGNodeStyleSetMaxHeight(ygNode, value.value)
        }

        is FlexValue.Relative -> {
            YGNodeStyleSetMaxHeightPercent(ygNode, value.pc)
        }

        null -> {}
    }
}