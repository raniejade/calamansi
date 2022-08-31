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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

object MainDispatcher : CoroutineDispatcher() {
    private lateinit var thread: Thread
    private val queue = LinkedBlockingQueue<java.lang.Runnable>()
    private var shouldShutdown = false

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return thread !== Thread.currentThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        queue.put(block)
    }

    fun shutdown() {
        shouldShutdown = true
    }

    fun loop() {
        thread = Thread.currentThread()

        while (!shouldShutdown) {
            queue.poll()?.run()
        }
    }
}

class RuntimeModule : Module(), InputContext {
    private var exitCode = 0
    private lateinit var window: Window
    private lateinit var gfx: Gfx
    @OptIn(DelicateCoroutinesApi::class)
    private val frameDispatcher = newSingleThreadContext("calamansi-frame")

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
            val scope = CoroutineScope(Dispatchers.Default)
            runBlocking {
                coroutineScope {
                    withContext(frameDispatcher) {
                        sceneModule.publishEvent(event)
                    }
                }
            }
        }

        window.show()
    }

    fun getExitCode(): Int = exitCode

    fun requestExit(exitCode: Int) {
        this.exitCode = exitCode
        window.closeWindow()
    }

    fun loop() {
        var lastTick = millis()
        var deltaMillis: Long

        val surface = gfx.createSurface(window)

        val t = Thread {
            runBlocking {
                do {
                    withContext(MainDispatcher) {
                        window.pollEvents()
                    }

                    withContext(frameDispatcher) {
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
            }

            MainDispatcher.shutdown()
        }
        t.name = "calamansi-sync"
        t.isDaemon = true
        t.start()

        MainDispatcher.loop()

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