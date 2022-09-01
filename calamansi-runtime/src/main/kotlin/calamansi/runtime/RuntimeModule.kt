package calamansi.runtime

import calamansi.input.InputContext
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.MouseButton
import calamansi.runtime.data.ProjectConfig
import calamansi.runtime.module.Module
import calamansi.runtime.resource.ResourceModule
import calamansi.runtime.sys.gfx.Gfx
import calamansi.runtime.sys.gfx.vulkan.VulkanGfxDriver
import calamansi.runtime.sys.window.Window
import calamansi.runtime.sys.window.glfw.GlfwWindowDriver
import kotlinx.coroutines.*
import kotlinx.serialization.json.decodeFromStream
import java.util.concurrent.TimeUnit

class RuntimeModule : Module(), InputContext {
    private var exitCode = 0
    private lateinit var window: Window
    private lateinit var gfx: Gfx

    private val publishedEvents = mutableListOf<Job>()

    // TODO: move to separate module?
    val projectConfig by lazy(this::loadProjectConfig)

    val sceneModule by lazy { getModule<SceneModule>() }

    override fun start() {
        logger.info { "Runtime module started." }
        val (width, height, title) = projectConfig
        GlfwWindowDriver.init()
        window = GlfwWindowDriver.create(width, height, title)

        VulkanGfxDriver.init()
        gfx = VulkanGfxDriver.create()

        window.registerEventHandler { event ->
            val job = GlobalScope.launch {
                coroutineScope {
                    launch(ScriptDispatcher) {
                        sceneModule.publishEvent(event)
                    }
                }
            }

            publishedEvents.add(job)
        }

        window.show()
    }

    fun getExitCode(): Int = exitCode

    fun requestExit(exitCode: Int) {
        this.exitCode = exitCode
        window.closeWindow()
    }

    suspend fun mainLoop() {
        var lastTick = millis()
        var deltaMillis: Long

        val surface = gfx.createSurface(window)

        do {
            withContext(MainDispatcher) {
                window.pollEvents()
                val copy = publishedEvents.toList()
                publishedEvents.clear()
                copy.joinAll()
            }

            withContext(ScriptDispatcher) {
                deltaMillis = (millis() - lastTick)
                lastTick = millis()
                frame(deltaMillis / 1000f).join()
            }

            withContext(MainDispatcher) {
                // for each renderable
                // render start
                val frame = gfx.startDraw(surface)

                // submit draw call
                // frame.draw(DrawMode.TRIANGLE)
            }

        } while (!window.shouldCloseWindow())

//        runBlocking {
//            do {
//                window.pollEvents()
//
//                deltaMillis = (millis() - lastTick)
//                lastTick = millis()
//                val job = frame(deltaMillis / 1000f)
//                var frameProcessed = false
//
//                while (!frameProcessed) {
//                    select<Unit> {
//                        // render frame
//                        job.onJoin {
//                            // for each renderable
//                            // render start
//                            val frame = gfx.startDraw(surface)
//
//                            // submit draw call
//                            // frame.draw(DrawMode.TRIANGLE)
//                            frameProcessed = true
//                        }
//                    }
//                }
//            } while (!window.shouldCloseWindow())
//        }
    }

    private suspend fun frame(delta: Float): Job {
        return coroutineScope {
            launch(Dispatchers.Default) {
                getModule<SceneModule>().frame(delta)
            }
        }
    }

    private fun millis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    override fun shutdown() {
        logger.info { "Runtime module shutting down." }
        window.destroy()
        GlfwWindowDriver.shutdown()
        VulkanGfxDriver.shutdown()
    }

    private fun loadProjectConfig(): ProjectConfig {
        val json = getModule<ResourceModule>().getJsonSerializer()
        // TODO: load via resource module
        val inputStream = checkNotNull(this::class.java.classLoader.getResourceAsStream("assets/project.cfg"))
        return json.decodeFromStream(inputStream)
    }

    override fun getKeyState(key: Key): InputState {
        return window.getKeyState(key)
    }

    override fun getMouseButtonState(button: MouseButton): InputState {
        return window.getMouseButtonState(button)
    }
}