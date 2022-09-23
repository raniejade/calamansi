package calamansi.runtime

import calamansi.runtime.data.ProjectConfig
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.service.Services
import calamansi.runtime.sys.glfw.GlfwWindowDriver
import calamansi.runtime.sys.opengl.OpenGLGfxDriver
import calamansi.runtime.threading.EventLoops
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

internal class Engine {
    private val logger = LoggerFactory.getLogger(Engine::class.java)
    private val registryService = Services.create(::RegistryService)
    private var frameCount = 0L

    private lateinit var mainWindowContext: WindowContext

    fun run(args: Array<String>) {
        logger.info("Starting engine.")
        val config = loadProjectConfig()
        setup(config)
        mainLoop()
        cleanup()
    }

    private fun mainLoop() {
        val orchestrator = Thread {
            var lastTick = millis()
            var deltaMillis: Long

            runCatching {
                EventLoops.Script.scheduleNow {
                    // load default scene
                }
            }

            do {
                mainWindowContext.pollEvents()

                // run scene
                deltaMillis = (millis() - lastTick)
                lastTick = millis()
                mainWindowContext.frame(deltaMillis, frameCount)

                mainWindowContext.render(frameCount)
                frameCount++
            } while (!mainWindowContext.shouldCloseWindow())
            EventLoops.Main.shutdown()
        }

        orchestrator.isDaemon = true
        orchestrator.name = "calamansi-orchestrator"
        orchestrator.start()
        EventLoops.Main.run()
    }

    private fun setup(config: ProjectConfig) {
        // services
        logger.info("Starting services.")
        registryService.start()

        logger.info("Setting up main window.")
        GlfwWindowDriver.start()
        OpenGLGfxDriver.start()
        val window = GlfwWindowDriver.create(config.width, config.height, config.title)
        mainWindowContext =
            WindowContext(
                window,
                OpenGLGfxDriver.create(window),
            )

        mainWindowContext.init()
    }

    private fun cleanup() {
        logger.info("Cleaning up.")
        mainWindowContext.destroy()
        OpenGLGfxDriver.stop()
        GlfwWindowDriver.stop()

        logger.info("Shutting down services.")
        registryService.stop()
    }

    private fun runCatching(cb: () -> Unit) {
        try {
            cb()
        } catch (e: Throwable) {
            logger.error("An error has occurred.", e)
        }
    }

    private fun millis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadProjectConfig(): ProjectConfig {
        logger.info("Loading project config.")
        val stream = javaClass.classLoader.getResourceAsStream("project.json") ?: return ProjectConfig()
        return Json.Default.decodeFromStream(stream)
    }
}

internal fun main(args: Array<String>) {
    Engine().run(args)
}