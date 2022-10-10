package calamansi.gfx

sealed class RenderSurface

@Deprecated(level = DeprecationLevel.ERROR, message = "not implemented")
class TextureRenderSurface(val width: Float, val height: Float) : RenderSurface()
object DefaultRenderSurface : RenderSurface()