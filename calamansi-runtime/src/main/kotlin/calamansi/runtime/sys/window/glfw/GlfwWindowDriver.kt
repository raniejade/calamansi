package calamansi.runtime.sys.window.glfw

import calamansi.runtime.sys.window.Window
import calamansi.runtime.sys.window.WindowDriver
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryUtil

object GlfwWindowDriver : WindowDriver {
    override fun init() {
        check(GLFW.glfwInit()) { "Failed to start GLFW. " }
    }

    override fun shutdown() {
        GLFW.glfwTerminate()
    }

    override fun create(width: Int, height: Int, title: String, createContext: Boolean): Window {
        check(!createContext) { "Context creation not supported!" }
        if (!createContext) {
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API)
        }
        val window = GLFW.glfwCreateWindow(
            width, height, title,
            MemoryUtil.NULL,
            MemoryUtil.NULL
        )

        check(window != MemoryUtil.NULL) { "Failed to create GLFW window." }

        return GlfwWindow(window, createContext)
    }
}