package calamansi.ui2.flex

import calamansi.runtime.gc.Bin
import calamansi.runtime.utils.StateTracker
import calamansi.ui2.control.Control
import calamansi.ui2.control.DimValue
import calamansi.ui2.control.FlatStyledBox
import org.lwjgl.util.yoga.Yoga.*

internal class YgControl(private val control: Control) {
    val ygNode = YGNodeNew()

    init {
        val localYgNode = ygNode
        Bin.register(this) {
            YGNodeFree(localYgNode)
        }
    }

    var alignContent: FlexAlign = FlexAlign.FLEX_START

    var alignItems: FlexAlign = FlexAlign.STRETCH

    var direction: FlexDirection = FlexDirection.ROW /* COLUMN */

    var justifyContent: FlexJustify = FlexJustify.FLEX_START

    var wrap: FlexWrap = FlexWrap.WRAP

    var layout: FlexLayout = FlexLayout.RELATIVE
    private val layoutState = StateTracker.create(
        control::minWidth,
        control::width,
        control::maxWidth,
        control::minHeight,
        control::height,
        control::maxHeight,
        control::paddingLeft,
        control::paddingTop,
        control::paddingRight,
        control::paddingBottom,
        control::marginLeft,
        control::marginTop,
        control::marginRight,
        control::marginBottom,
        this::alignContent,
        this::alignItems,
        this::direction,
        this::justifyContent,
        this::wrap,
        this::layout,
    )

    fun layout(forceLayout: Boolean) {
        if (!layoutState.isDirty() && !forceLayout) {
            return
        }

        YGNodeStyleSetFlexDirection(ygNode, direction.toYGValue())
        YGNodeStyleSetAlignItems(ygNode, alignItems.toYGValue())
        YGNodeStyleSetAlignContent(ygNode, alignContent.toYGValue())
        YGNodeStyleSetJustifyContent(ygNode, justifyContent.toYGValue())
        YGNodeStyleSetAlignSelf(ygNode, FlexAlign.AUTO.toYGValue())

        YGNodeStyleSetPositionType(
            ygNode,
            when (layout) {
                FlexLayout.RELATIVE -> YGPositionTypeRelative
                FlexLayout.ABSOLUTE -> YGPositionTypeAbsolute
            }
        )

        YGNodeStyleSetFlexWrap(ygNode, wrap.toYGValue())

        applyStyleWidth(ygNode, DimValue.Fixed(control.layoutWidth))
        applyStyleHeight(ygNode, DimValue.Fixed(control.layoutHeight))

        //applyStyleMinWidth(ygNode, control.minWidth)
        //applyStyleMinHeight(ygNode, control.minHeight)

        //applyStyleMaxWidth(ygNode, control.maxWidth)
        //applyStyleMaxHeight(ygNode, control.maxHeight)

        applyStyleMargin(ygNode, DimValue.Fixed(control.marginLeft), YGEdgeLeft)
        applyStyleMargin(ygNode, DimValue.Fixed(control.marginTop), YGEdgeTop)
        applyStyleMargin(ygNode, DimValue.Fixed(control.marginRight), YGEdgeRight)
        applyStyleMargin(ygNode, DimValue.Fixed(control.marginBottom), YGEdgeBottom)

        applyStylePadding(ygNode, DimValue.Fixed(control.paddingLeft), YGEdgeLeft)
        applyStylePadding(ygNode, DimValue.Fixed(control.paddingTop), YGEdgeTop)
        applyStylePadding(ygNode, DimValue.Fixed(control.paddingRight), YGEdgeRight)
        applyStylePadding(ygNode, DimValue.Fixed(control.paddingBottom), YGEdgeBottom)

        val styledBox = control.styledBox
        if (styledBox is FlatStyledBox) {
            YGNodeStyleSetBorder(ygNode, YGEdgeLeft, styledBox.borderLeftWidth)
            YGNodeStyleSetBorder(ygNode, YGEdgeTop, styledBox.borderTopWidth)
            YGNodeStyleSetBorder(ygNode, YGEdgeRight, styledBox.borderRightWidth)
            YGNodeStyleSetBorder(ygNode, YGEdgeBottom, styledBox.borderBottomWidth)
        }
    }

    fun applyLayoutValues() {
        control.layoutX = YGNodeLayoutGetLeft(ygNode)
        control.layoutY = YGNodeLayoutGetTop(ygNode)
        control.layoutWidth = YGNodeLayoutGetWidth(ygNode)
        control.layoutHeight = YGNodeLayoutGetHeight(ygNode)
    }
}