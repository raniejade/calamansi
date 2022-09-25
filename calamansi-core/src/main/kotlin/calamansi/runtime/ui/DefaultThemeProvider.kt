package calamansi.runtime.ui

import calamansi.gfx.Color
import calamansi.meta.CalamansiInternal
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.service.Services
import calamansi.ui.*
import org.joml.Vector4f

internal object DefaultThemeProvider {
    private val resourceService by Services.get<ResourceService>()

    @OptIn(CalamansiInternal::class)
    fun create(): Theme {
        val defaultFont = resourceService.loadResource("rt://OpenSans-Regular.ttf", Font::class, 0) as Font
        return ThemeBuilder()
            // CanvasElement
            .addStyledBox(CanvasElement::class, "normal", EmptyStyledBox())
            .addStyledBox(CanvasElement::class, "pressed", EmptyStyledBox())
            .addStyledBox(CanvasElement::class, "hovered", EmptyStyledBox())

            // Text
            .addStyledBox(Text::class, "normal", EmptyStyledBox())
            .addStyledBox(Text::class, "pressed", EmptyStyledBox())
            .addStyledBox(Text::class, "hovered", EmptyStyledBox())
            .addFont(Text::class, "font", defaultFont)
            .addConstant(Text::class, "fontSize", 12f)
            .addColor(Text::class, "fontColor", Color.WHITE)

            // Button
            .addStyledBox(Button::class, "normal", FlatStyledBox().apply { backgroundColor = Color("ffb4be56"); borderRadius = Vector4f(5f) })
            .addStyledBox(Button::class, "pressed", FlatStyledBox().apply { backgroundColor = Color("ffa0ab41"); borderRadius = Vector4f(6f) })
            .addStyledBox(Button::class, "hovered", FlatStyledBox().apply { backgroundColor = Color("ffc6ce80"); borderRadius = Vector4f(5f) })
            .addFont(Button::class, "font", defaultFont)
            .addConstant(Button::class, "fontSize", 12f)
            .addColor(Button::class, "fontColor", Color.WHITE)
            .build()
    }
}