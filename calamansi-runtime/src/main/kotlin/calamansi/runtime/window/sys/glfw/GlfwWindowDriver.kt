package calamansi.runtime.window.sys.glfw

import calamansi.runtime.window.sys.Window
import calamansi.runtime.window.sys.WindowDriver
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryUtil

object GlfwWindowDriver : WindowDriver {
    override fun start() {
        check(GLFW.glfwInit()) { "Failed to start GLFW. " }
    }

    override fun stop() {
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