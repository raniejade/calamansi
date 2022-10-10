package calamansi.ui2.flex

import calamansi.runtime.utils.StateTracker
import calamansi.ui2.control.Container
import calamansi.ui2.control.Control
import calamansi.ui2.control.DimValue
import calamansi.ui2.control.FlatStyledBox
import org.lwjgl.util.yoga.Yoga.*
import java.util.*

class FlexContainer : Container() {
    var alignContent: FlexAlign = FlexAlign.STRETCH
    var alignItems: FlexAlign = FlexAlign.STRETCH
    var direction: FlexDirection = FlexDirection.ROW /* COLUMN */
    var justifyContent: FlexJustify = FlexJustify.FLEX_START
    var wrap: FlexWrap = FlexWrap.WRAP
    var layout: FlexLayout = FlexLayout.RELATIVE

    private var ygNode = YGNodeNew()
    private val layoutState = StateTracker.create(
        this::minWidth,
        this::width,
        this::maxWidth,
        this::minHeight,
        this::height,
        this::maxHeight,
        this::paddingLeft,
        this::paddingTop,
        this::paddingRight,
        this::paddingBottom,
        this::marginLeft,
        this::marginTop,
        this::marginRight,
        this::marginBottom,
        this::alignContent,
        this::alignItems,
        this::direction,
        this::justifyContent,
        this::wrap,
        this::layout,
        this::styledBox,
    )
    private val childrenState = StateTracker.create(this::children)
    private val childrenNodes = WeakHashMap<Control, YgControl>()

    override fun layout(width: Float, height: Float, forceLayout: Boolean) {
        super.layout(width, height, forceLayout)

        var isDirty = false
        if (childrenState.isDirty() || forceLayout) {
            isDirty = true
            YGNodeRemoveAllChildren(ygNode)
            for ((idx, child) in children.withIndex()) {
                val node = childrenNodes.getOrPut(child) { YgControl(child) }
                YGNodeInsertChild(ygNode, node.ygNode, idx)
            }
        }

        if (layoutState.isDirty() || forceLayout) {
            isDirty = true
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

            //applyStylePosition(ygNode, position.left, YGEdgeLeft)
            //applyStylePosition(ygNode, position.top, YGEdgeTop)
            //applyStylePosition(ygNode, position.bottom, YGEdgeBottom)
            //applyStylePosition(ygNode, position.right, YGEdgeRight)
            // TODO: account for user defined width and height
            applyStyleWidth(ygNode, DimValue.Fixed(width))
            applyStyleHeight(ygNode, DimValue.Fixed(height))

            applyStyleMinWidth(ygNode, this.minWidth)
            applyStyleMinHeight(ygNode, this.minHeight)

            applyStyleMaxWidth(ygNode, this.maxWidth)
            applyStyleMaxHeight(ygNode, this.maxHeight)

            applyStyleMargin(ygNode, DimValue.Fixed(marginLeft), YGEdgeLeft)
            applyStyleMargin(ygNode, DimValue.Fixed(marginTop), YGEdgeTop)
            applyStyleMargin(ygNode, DimValue.Fixed(marginRight), YGEdgeRight)
            applyStyleMargin(ygNode, DimValue.Fixed(marginBottom), YGEdgeBottom)

            applyStylePadding(ygNode, DimValue.Fixed(paddingLeft), YGEdgeLeft)
            applyStylePadding(ygNode, DimValue.Fixed(paddingTop), YGEdgeTop)
            applyStylePadding(ygNode, DimValue.Fixed(paddingRight), YGEdgeRight)
            applyStylePadding(ygNode, DimValue.Fixed(paddingBottom), YGEdgeBottom)

            val styledBox = styledBox
            if (styledBox is FlatStyledBox) {
                YGNodeStyleSetBorder(ygNode, YGEdgeLeft, styledBox.borderLeftWidth)
                YGNodeStyleSetBorder(ygNode, YGEdgeTop, styledBox.borderTopWidth)
                YGNodeStyleSetBorder(ygNode, YGEdgeRight, styledBox.borderRightWidth)
                YGNodeStyleSetBorder(ygNode, YGEdgeBottom, styledBox.borderBottomWidth)
            }
        }

        if (isDirty) {
            // TODO: re-calculate if child has become dirty
            for (child in children) {
                childrenNodes.getValue(child).layout(forceLayout)
            }

            YGNodeCalculateLayout(ygNode, YGUndefined, YGUndefined, YGDirectionLTR)

            layoutX = YGNodeLayoutGetLeft(ygNode)
            layoutY = YGNodeLayoutGetTop(ygNode)
            layoutWidth = YGNodeLayoutGetWidth(ygNode)
            layoutHeight = YGNodeLayoutGetHeight(ygNode)

            for (child in children) {
                childrenNodes.getValue(child).applyLayoutValues()
            }
        }
    }
}