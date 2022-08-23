package calamansi.runtime

import calamansi.runtime.data.ProjectConfig
import calamansi.runtime.module.Module
import calamansi.runtime.resource.ResourceModule
import kotlinx.serialization.json.decodeFromStream
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL
import java.util.concurrent.TimeUnit

class RuntimeModule : Module() {
    private var window = NULL
    private var exitCode = 0
    // TODO: move to separate module?
    val projectConfig by lazy(this::loadProjectConfig)

    override fun start() {
        logger.info { "Runtime module started." }

        // init glfw
        check(glfwInit()) { "Failed to start GLFW. " }
        glfwDefaultWindowHints()
        window = glfwCreateWindow(projectConfig.width, projectConfig.height, projectConfig.title, NULL, NULL)
        check(window != NULL) { "Failed to create window. " }
        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true)
            }
        }
        // Get the thread stack and push a new frame
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val videoMode = checkNotNull(glfwGetVideoMode(glfwGetPrimaryMonitor()))

            // Center the window
            glfwSetWindowPos(
                window,
                (videoMode.width() - pWidth[0]) / 2,
                (videoMode.height() - pHeight[0]) / 2
            )
        }
        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)
        // Make the window visible
        glfwShowWindow(window)
    }

    fun getExitCode(): Int = exitCode

    fun requestExit(exitCode: Int) {
        this.exitCode = exitCode
        glfwSetWindowShouldClose(window, true)
    }

    fun loop() {
        var lastTick = millis()
        var deltaMillis: Long

        do {
            deltaMillis = (millis() - lastTick)
            lastTick = millis()
            frame(deltaMillis / 1000f)

            glfwSwapBuffers(window)// swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        } while (!glfwWindowShouldClose(window))
    }

    private fun frame(delta: Float) {
        getModule<SceneModule>().frame(delta)
    }

    private fun millis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    override fun shutdown() {
        logger.info { "Runtime module shutting down." }
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
    }

    private fun loadProjectConfig(): ProjectConfig {
        val json = getModule<ResourceModule>().getJsonSerializer()
        // TODO: load via resource module
        val inputStream = checkNotNull(this::class.java.classLoader.getResourceAsStream("assets/project.cfg"))
        return json.decodeFromStream(inputStream)
    }
}