package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.Gfx
import calamansi.runtime.sys.GfxDriver
import calamansi.runtime.sys.Window
import calamansi.runtime.sys.glfw.GlfwWindow
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.opengl.GL

object OpenGLGfxDriver : GfxDriver {
    override fun start() {
        // nada
    }

    override fun create(window: Window): Gfx {
        if (window is GlfwWindow) {
            glfwMakeContextCurrent(window.handle)
            GL.createCapabilities()
        }
        return OpenGLGfx(window)
    }

    override fun stop() {
        // nada
    }
}