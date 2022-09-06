package calamansi.runtime.window

import calamansi.runtime.service.Service
import calamansi.runtime.window.sys.Window
import calamansi.runtime.window.sys.glfw.GlfwWindowDriver

class WindowService : Service {
    private val driver = GlfwWindowDriver

    override fun start() {
        driver.start()
    }

    fun createWindow(width: Int, height: Int, title: String = ""): Window {
        return driver.create(width, height, title)
    }

    override fun stop() {
        driver.stop()
    }
}