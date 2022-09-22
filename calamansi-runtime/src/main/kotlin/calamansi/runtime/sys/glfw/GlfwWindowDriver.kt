package calamansi.runtime.sys.glfw

import calamansi.runtime.sys.Window
import calamansi.runtime.sys.WindowDriver
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil

object GlfwWindowDriver : WindowDriver {
    override fun start() {
        check(glfwInit()) { "Failed to start GLFW. " }
    }

    override fun stop() {
        glfwTerminate()
    }

    override fun create(width: Int, height: Int, title: String, createContext: Boolean): Window {
        if (!createContext) {
            glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        } else {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        }
        glfwInitHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE)
        val window = glfwCreateWindow(
            width, height, title,
            MemoryUtil.NULL,
            MemoryUtil.NULL
        )

        check(window != MemoryUtil.NULL) { "Failed to create GLFW window." }

        return GlfwWindow(window, createContext)
    }
}