package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.RenderTarget
import org.jetbrains.skija.*

internal class SkijaContext private constructor(
    private val renderTarget: BackendRenderTarget,
    private val surface: Surface,
) {
    fun destroy() {
        surface.close()
        renderTarget.close()
    }

    fun draw(body: Canvas.() -> Unit) {
        context.resetAll()
        body(surface.canvas)
        surface.flushAndSubmit(true)
    }

    companion object {
        private lateinit var context: DirectContext

        fun create(width: Int, height: Int, fbo: Int): SkijaContext {
            if (!Companion::context.isInitialized) {
                context = DirectContext.makeGL()
            }

            val renderTarget = BackendRenderTarget.makeGL(
                width,
                height,
                /*samples*/0,
                /*stencil*/8,
                /*fbId*/ fbo,
                FramebufferFormat.GR_GL_RGBA8
            )

            val surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.getDisplayP3(),  // TODO load monitor profile
                null
            )

            return SkijaContext(renderTarget, surface)
        }
    }
}