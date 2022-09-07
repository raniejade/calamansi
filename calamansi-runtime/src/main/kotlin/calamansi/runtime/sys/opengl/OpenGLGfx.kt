package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.Gfx
import calamansi.runtime.sys.Window
import calamansi.runtime.sys.glfw.GlfwWindow
import org.lwjgl.glfw.GLFW.glfwSwapBuffers

class OpenGLGfx(private val window: Window) : Gfx {
    override fun swapBuffers() {
        if (window is GlfwWindow) {
            glfwSwapBuffers(window.handle)
        }
    }
}