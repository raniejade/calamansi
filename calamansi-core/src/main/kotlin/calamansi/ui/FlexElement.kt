package calamansi.ui

interface FlexElement {
    var alignContent: FlexAlign

    var alignItems: FlexAlign 

    var direction: FlexDirection 

    var justifyContent: FlexJustify 

    var wrap: FlexWrap

    var layout: FlexLayout 

    var position: FlexBounds 

    var margin: FlexBounds

    var padding: FlexBounds 

    var alignSelf: FlexAlign 

    var grow: Float

    var shrink: Float

    var basis: FlexValue?

    var width: FlexValue? 

    var height: FlexValue?

    var minWidth: FlexValue?

    var minHeight: FlexValue?

    var maxWidth: FlexValue?

    var maxHeight: FlexValue?

    var font: FontValue
}