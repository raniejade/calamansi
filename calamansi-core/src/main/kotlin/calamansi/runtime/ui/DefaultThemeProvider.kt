package calamansi.runtime.ui

import calamansi.gfx.Color
import calamansi.meta.CalamansiInternal
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.service.Services
import calamansi.ui.*

internal object DefaultThemeProvider {
    private val resourceService by Services.get<ResourceService>()

    @OptIn(CalamansiInternal::class)
    fun create(): Theme {
        val defaultFont = resourceService.loadResource("rt://OpenSans-Regular.ttf", Font::class, 0) as Font
        return ThemeBuilder()
            // CanvasElement
            .addStyledBox(CanvasElement::class, "normal", EmptyStyledBox())
            .addStyledBox(CanvasElement::class, "hovered", EmptyStyledBox())
            .addConstant(CanvasElement::class, "minWidth", 0f)
            .addConstant(CanvasElement::class, "minHeight", 0f)

            // Text
            .addStyledBox(Text::class, "normal", EmptyStyledBox())
            .addStyledBox(Text::class, "hovered", EmptyStyledBox())
            .addFont(Text::class, "font", defaultFont)
            .addConstant(Text::class, "fontSize", 16f)
            .addColor(Text::class, "fontColor", Color.WHITE)
            .addConstant(Text::class, "minWidth", 0f)
            .addConstant(Text::class, "minHeight", 0f)

            // TextInput
            .addStyledBox(TextInput::class, "normal", FlatStyledBox().apply {
                backgroundColor = Color.WHITE
                borderRadius = Corner(2f)
                borderWidth = Box(2f)
                borderColor = Color("ff90993a")
            })
            .addStyledBox(TextInput::class, "hovered", FlatStyledBox().apply {
                backgroundColor = Color.WHITE
                borderRadius = Corner(2f)
                borderWidth = Box(2f)
                borderColor = Color("ff90993a")
            })
            .addFont(TextInput::class, "font", defaultFont)
            .addConstant(TextInput::class, "fontSize", 16f)
            .addColor(TextInput::class, "fontColor", Color.BLUE)
            .addConstant(TextInput::class, "minWidth", 100f)
            .addConstant(TextInput::class, "minHeight", 25f)

            // Button
            .addStyledBox(
                Button::class,
                "normal",
                FlatStyledBox().apply {
                    backgroundColor = Color("ffb4be56")
                    borderRadius = Corner(5f)
                    borderWidth = Box(bottom = 2f)
                    borderColor = Color("ff90993a")
                })
            .addStyledBox(
                Button::class,
                "pressed",
                FlatStyledBox().apply {
                    backgroundColor = Color("ffa0ab41")
                    borderRadius = Corner(6f)
                })
            .addStyledBox(
                Button::class,
                "hovered",
                FlatStyledBox().apply {
                    backgroundColor = Color("ffc6ce80")
                    borderRadius = Corner(5f)
                    borderWidth = Box(bottom = 2f)
                    borderColor = Color("ff90993a")
                })
            .addFont(Button::class, "font", defaultFont)
            .addConstant(Button::class, "fontSize", 16f)
            .addColor(Button::class, "fontColor", Color.WHITE)
            .addConstant(Button::class, "minWidth", 50f)
            .addConstant(Button::class, "minHeight", 25f)
            .build()
    }
}