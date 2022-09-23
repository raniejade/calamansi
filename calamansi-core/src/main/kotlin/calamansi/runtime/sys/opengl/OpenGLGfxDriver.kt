package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.Gfx
import calamansi.runtime.sys.GfxDriver
import calamansi.runtime.sys.Window
import calamansi.runtime.sys.glfw.GlfwWindow
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.opengl.GL

internal object OpenGLGfxDriver : GfxDriver {
    override fun start() {
        // nada
    }

    override fun create(window: Window): Gfx {
        require(window is GlfwWindow)
        glfwMakeContextCurrent(window.handle)
        val capabilities = GL.createCapabilities()
        glfwMakeContextCurrent(0)
        return OpenGLGfx(window, capabilities)
    }

    override fun stop() {
        // nada
    }
}