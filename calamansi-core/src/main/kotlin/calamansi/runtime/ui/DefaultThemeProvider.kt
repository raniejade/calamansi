package calamansi.runtime.ui

import calamansi.gfx.Color
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.service.Services
import calamansi.gfx.Font
import calamansi.ui2.control.*
import calamansi.ui2.flex.FlexContainer

internal object DefaultThemeProvider {
    private val resourceService by Services.get<ResourceService>()

    fun create(): Theme {
        val defaultFont = resourceService.loadResource("rt://OpenSans-Regular.ttf", Font::class, 0) as Font
        return ThemeBuilder()
            .element<Control> {
                styledBox("defaultStyledBox", EmptyStyledBox())
                styledBox("hoveredStyledBox", EmptyStyledBox())
            }

            .element<Container>(Control::class) {
                // nada
            }

            .element<FlexContainer>(Container::class) {
                // nada
            }

            .element<Text>(Control::class) {
                styledBox("defaultStyledBox", FlatStyledBox().apply {
                    backgroundColor = Color.TRANSPARENT
                    setBorderWidth(1f)
                    setBorderRadius(5f)
                    borderColor = Color.GREEN
                })
                font("font", defaultFont)
                float("fontSize", 16f)
                color("fontColor", Color.WHITE)
            }

//            .element<TextBase>(CanvasElement::class) {
//                font("font", defaultFont)
//                float("fontSize", 16f)
//                color("fontColor", Color.WHITE)
//            }
//
//            .element<Text>(TextBase::class) {
//                // no overrides for now
//            }
//
//            .element<TextArea>(TextBase::class) {
//                styledBox("defaultStyledBox", FlatStyledBox().apply {
//                    backgroundColor = Color.WHITE
//                    borderRadius = Corner(2f)
//                    borderWidth = Box(2f)
//                    borderColor = Color("ff90993a")
//                })
//
//                styledBox("hoveredStyledBox", FlatStyledBox().apply {
//                    backgroundColor = Color.WHITE
//                    borderRadius = Corner(2f)
//                    borderWidth = Box(2f)
//                    borderColor = Color("ff90993a")
//                })
//
//                color("fontColor", Color.BLUE)
//            }
//
//            .element<Button>(TextBase::class) {
//                styledBox("defaultStyledBox", FlatStyledBox().apply {
//                    backgroundColor = Color("ffb4be56")
//                    borderRadius = Corner(5f)
//                    borderWidth = Box(bottom = 2f)
//                    borderColor = Color("ff90993a")
//                })
//
//                styledBox("hoveredStyledBox", FlatStyledBox().apply {
//                    backgroundColor = Color("ffc6ce80")
//                    borderRadius = Corner(5f)
//                    borderWidth = Box(bottom = 2f)
//                    borderColor = Color("ff90993a")
//                })
//
//                styledBox("pressedStyledBox", FlatStyledBox().apply {
//                    backgroundColor = Color("ffa0ab41")
//                    borderRadius = Corner(6f)
//                })
//            }

            .build()
    }
}