package calamansi.runtime

import calamansi.node.Scene
import calamansi.resource.ResourceRef
import calamansi.runtime.data.ProjectConfig
import calamansi.runtime.font.FontService
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.resource.source.JarFileSource
import calamansi.runtime.resource.source.RelativeFileSource
import calamansi.runtime.service.Services
import calamansi.runtime.sys.glfw.GlfwWindowDriver
import calamansi.runtime.sys.opengl.OpenGLGfxDriver
import calamansi.runtime.threading.EventLoops
import calamansi.runtime.utils.FrameStats
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

internal class Engine {
    private val logger = LoggerFactory.getLogger(Engine::class.java)
    private val registryService = Services.create(::RegistryService)
    private val resourceService = Services.create(::ResourceService)
    private val fontService = Services.create(::FontService)
    private var frameCount = 0L
    private val frameStats = FrameStats(1000)

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
            var lastTick = nanoTime()
            var deltaNano: Long

            runCatching {
                EventLoops.Script.scheduleNow {
                    // load default scene
                    val scene =
                        resourceService.loadResource("assets://default.scn.json", Scene::class, 0) as ResourceRef<Scene>
                    mainWindowContext.setScene(scene)
                }
            }

            do {
                // run scene
                deltaNano = (nanoTime() - lastTick)
                lastTick = nanoTime()
                val delta = TimeUnit.NANOSECONDS.toMillis(deltaNano).toFloat()
                mainWindowContext.frame(delta, frameStats.frameNo)
                mainWindowContext.render(frameStats.frameNo)
                frameStats.frame(delta)

                mainWindowContext.pollEvents()
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
        resourceService.start()
        fontService.start()

        logger.info("Registering file sources.")
        resourceService.registerSource(
            "assets",
            RelativeFileSource("assets", JarFileSource(this::class.java.classLoader))
        )

        // TODO: make private (not accessible by app)
        resourceService.registerSource(
            "rt",
            RelativeFileSource("rt", JarFileSource(this::class.java.classLoader))
        )

        logger.info("Setting up main window.")
        GlfwWindowDriver.start()
        OpenGLGfxDriver.start()
        val window = GlfwWindowDriver.create(config.width, config.height, config.title)
        mainWindowContext =
            WindowContext(
                window,
                OpenGLGfxDriver.create(window),
                frameStats,
            )

        mainWindowContext.init()
    }

    private fun cleanup() {
        logger.info("Cleaning up.")
        mainWindowContext.destroy()
        OpenGLGfxDriver.stop()
        GlfwWindowDriver.stop()

        logger.info("Shutting down services.")
        fontService.stop()
        resourceService.stop()
        registryService.stop()
    }

    private fun runCatching(cb: () -> Unit) {
        try {
            cb()
        } catch (e: Throwable) {
            logger.error("An error has occurred.", e)
        }
    }

    private fun nanoTime() = System.nanoTime()

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