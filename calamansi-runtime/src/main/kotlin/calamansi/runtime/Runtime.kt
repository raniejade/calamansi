package calamansi.runtime

import calamansi.runtime.io.FileSystem
import calamansi.runtime.io.JarFileSource
import calamansi.runtime.io.NotImplementedFileSource
import calamansi.runtime.logging.ConsoleLogger
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.registry.RuntimeRegistry
import calamansi.runtime.serializer.Serializer
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

class Runtime {
    private val logger = ConsoleLogger(LogLevel.DEBUG)
    private val entryLoader = ServiceLoader.load(Entry::class.java)
    private val registry = RuntimeRegistry(logger)
    private val filesystem = FileSystem(JarFileSource(this::class.java.classLoader), NotImplementedFileSource)
    private val componentManager = ComponentManager(registry)
    private val scriptManager = ScriptManager(registry)
    private val executionContext = ExecutionContextImpl(logger, componentManager, scriptManager)
    private val nodeManager = NodeManager(componentManager, scriptManager)
    private val sceneManager = SceneManager()
    private lateinit var serializer: Serializer

    @OptIn(ExperimentalStdlibApi::class)
    fun execute() {
        val entry = entryLoader.findFirst().getOrNull()
        if (entry != null) {
            execute(entry)
        } else {
            println("No entry implementation loaded")
        }
    }

    private fun execute(entry: Entry) {
        logger.info { "Bootstrapping engine using ${entry::class.qualifiedName}." }
        entry.bootstrap(registry)
        serializer = Serializer(registry.serializersModule)
        bootstrapInitialScene()
        loop()
        cleanup()
    }

    private fun bootstrapInitialScene() {
        val defaultScene = getDefaultScene()
        logger.info { "Loading default scene: $defaultScene." }
        try {
            val scene = serializer.decodeScene(filesystem.getReader("res://$defaultScene"))
            with(executionContext) {
                val root = nodeManager.buildSceneGraph(scene)
                if (root == null) {
                    logger.warn { "Failed to build scene graph!" }
                }
                sceneManager.setCurrentScene(root)
            }
        } catch (e: IOException) {
            logger.error(e) { "Failed to load default scene." }
        }
    }

    private fun getDefaultScene() = "default.scn"

    private fun loop() {
        var lastTick = millis()
        var deltaMillis: Long

        do {
            deltaMillis = (millis() - lastTick)
            lastTick = millis()
            frame(deltaMillis / 1000f)
        } while (!shouldExit())
    }

    private fun frame(delta: Float) {
        with(executionContext) {
            sceneManager.frame(delta)
        }

        Thread.sleep(10)
    }

    private fun cleanup() {
        logger.info { "Shutting down, initiating cleanup procedures." }
        with(executionContext) {
            sceneManager.setCurrentScene(null)
        }
    }

    private fun shouldExit(): Boolean {
        return false
    }

    private fun millis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}

fun main(vararg args: String) {
    Runtime().execute()
}