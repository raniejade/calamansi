package calamansi.runtime

import calamansi.Scene
import calamansi.resource.ResourceRef
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import calamansi.runtime.resource.ResourceModule
import calamansi.runtime.resource.SceneLoader
import calamansi.runtime.resource.source.JarFileSource
import calamansi.runtime.resource.source.RelativeFileSource
import calamansi.runtime.scripting.ScriptModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess


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
        Thread.currentThread().name = "calamansi-main"
        start()

        val t = Thread {
            runBlocking {
                // load default scene
                val defaultScene = runtimeModule.projectConfig.defaultScene
                withContext(ScriptDispatcher) {
                    sceneModule.setCurrentScene(resourceModule.fetchResource(defaultScene) as ResourceRef<Scene>)
                }
                runtimeModule.mainLoop()
                sceneModule.unloadCurrentScene()
            }
            MainDispatcher.shutdown()
        }

        t.name = "calamansi-sync"
        t.isDaemon = true
        t.start()
        MainDispatcher.loop()

        shutdown()
    }

    private fun start() {
        Module.configureLogging(LogLevel.DEBUG)
        modules.forEach(Module::start)
        registryModule.pushContext(this::class.java.classLoader)

        // resource sources
        logger.info { "Registering file sources." }
        resourceModule.registerSource(
            "assets",
            RelativeFileSource("assets", JarFileSource(this::class.java.classLoader))
        )

        // resource loaders
        logger.info { "Registering resource loaders." }
        resourceModule.registerLoader(SceneLoader())
    }

    private fun shutdown() {
        registryModule.popContext()
        val exitCode = runtimeModule.getExitCode()
        modules.reversed().forEach(Module::shutdown)
        exitProcess(exitCode)
    }
}

fun main(vararg args: String) {
    Engine().run()
}