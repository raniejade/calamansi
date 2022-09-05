package calamansi.runtime

import calamansi.Scene
import calamansi.resource.ResourceRef
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.logging.LoggingService
import calamansi.runtime.model.ProjectConfig
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.resource.loader.SceneLoader
import calamansi.runtime.resource.source.JarFileSource
import calamansi.runtime.resource.source.RelativeFileSource
import calamansi.runtime.window.WindowService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.concurrent.TimeUnit

class Engine {
    private val loggingService = Services.create(::LoggingService)
    private val registryService = Services.create(::RegistryService)
    private val resourceService = Services.create(::ResourceService)
    private val windowService = Services.create(::WindowService)
    private val logger by lazy { loggingService.getLogger(Engine::class) }
    private lateinit var mainWindowContext: WindowContext

    fun run() {
        // load project.cfg
        val projectConfig = loadProjectConfig()
        startServices(projectConfig.logLevel)

        // create main window
        val mainWindow = windowService.createWindow(projectConfig.width, projectConfig.height, projectConfig.title)
        mainWindowContext = WindowContext(mainWindow)

        mainLoop()

        stopServices()
    }

    private fun mainLoop() {
        val orchestrator = Thread {
            var lastTick = millis()
            var deltaMillis: Long

            runCatching {
                EventLoops.Script.scheduleNow {
                    // load default scene
                    val defaultScene = resourceService.loadResource("assets://default.scn") as ResourceRef<Scene>
                    mainWindowContext.setScene(defaultScene)
                }
            }

            do {
                runCatching {
                    // events (main thread - polling), events should be published to scripting thread.
                    EventLoops.Main.scheduleNow {
                        mainWindowContext.pollEvents()
                    }


                    // dispatch script (script thread)
                    EventLoops.Script.scheduleNow {
                        deltaMillis = (millis() - lastTick)
                        lastTick = millis()
                        mainWindowContext.onUpdate(deltaMillis / 1000)
                    }

                    // draw (main thread)
                    EventLoops.Main.scheduleNow {

                    }
                }
            } while (!mainWindowContext.shouldCloseWindow())
            EventLoops.Main.shutdown()
        }

        orchestrator.isDaemon = true
        orchestrator.name = "calamansi-orchestrator"
        orchestrator.start()
        // block main thread and run an event loop
        EventLoops.Main.run()
    }

    private fun runCatching(cb: () -> Unit) {
        try {
            cb()
        } catch (e: Throwable) {
            logger.error(e) { "An error has occurred." }
        }
    }

    private fun startServices(logLevel: LogLevel) {
        loggingService.start()
        loggingService.configureLogging(logLevel)

        logger.info { "Starting services." }

        registryService.start()
        resourceService.start()
        windowService.start()

        // resource sources
        logger.info { "Registering file sources." }
        resourceService.registerSource(
            "assets",
            RelativeFileSource("assets", JarFileSource(this::class.java.classLoader))
        )

        // resource loaders
        logger.info { "Registering resource loaders." }
        resourceService.registerLoader(SceneLoader())
    }

    private fun stopServices() {
        logger.info { "Stopping services." }
        windowService.stop()
        resourceService.stop()
        registryService.stop()
        loggingService.stop()
    }

    private fun millis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    private fun loadProjectConfig(): ProjectConfig {
        val stream = javaClass.classLoader.getResourceAsStream("project.cfg") ?: return ProjectConfig()
        return Json.Default.decodeFromStream(stream)
    }
}

fun main() {
    Engine().run()
}