package calamansi.runtime

import calamansi.Scene
import calamansi.resource.ResourceRef
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.logging.LoggerModule
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import calamansi.runtime.resource.ResourceModule
import calamansi.runtime.resource.SceneLoader
import calamansi.runtime.resource.source.JarFileSource
import calamansi.runtime.resource.source.RootedFileSource
import calamansi.runtime.scripting.ScriptModule
import java.util.concurrent.TimeUnit

class Engine {
    private val registryModule = RegistryModule()
    private val resourceModule = ResourceModule()
    private val sceneModule = SceneModule()
    private val scriptModule = ScriptModule()
    private val runtimeModule = RuntimeModule()

    private val modules = listOf(
        registryModule,
        resourceModule,
        sceneModule,
        scriptModule,
        runtimeModule,
    )

    private val logger by lazy {
        Module.getLogger(this::class)
    }

    fun run() {
        start()
        mainLoop()
        shutdown()
    }

    private fun start() {
        Module.configureLogging(LogLevel.INFO)
        modules.forEach(Module::start)
        registryModule.pushContext(this::class.java.classLoader)

        // resource sources
        logger.info { "Registering file sources." }
        resourceModule.registerSource(
            "assets",
            RootedFileSource("assets", JarFileSource(this::class.java.classLoader))
        );

        // resource loaders
        logger.info { "Registering resource loaders." }
        resourceModule.registerLoader(SceneLoader())

        // load default scene
        val defaultScene = getDefaultScene()
        logger.info { "Using default scene: $defaultScene" }
        sceneModule.setCurrentScene(resourceModule.loadResource(defaultScene) as ResourceRef<Scene>)
    }

    private fun shutdown() {
        sceneModule.unloadCurrentScene()
        registryModule.popContext()
        modules.forEach(Module::shutdown)
    }

    private fun getDefaultScene() = "assets://default.scn"

    private fun mainLoop() {
        var lastTick = millis()
        var deltaMillis: Long

        do {
            deltaMillis = (millis() - lastTick)
            lastTick = millis()
            frame(deltaMillis / 1000f)
        } while (!runtimeModule.shouldExit())
    }

    private fun frame(delta: Float) {
        sceneModule.frame(delta)
    }

    private fun millis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}

fun main(vararg args: String) {
    Engine().run()
}