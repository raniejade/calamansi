package calamansi.runtime

import calamansi.font.Font
import calamansi.node.Scene
import calamansi.resource.ResourceRef
import calamansi.runtime.font.FontImpl
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.logging.LoggingService
import calamansi.runtime.model.ProjectConfig
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.resource.source.JarFileSource
import calamansi.runtime.resource.source.RelativeFileSource
import calamansi.runtime.sys.*
import calamansi.runtime.sys.opengl.OpenGLGfxDriver
import calamansi.runtime.sys.opengl.checkOpenGLError
import calamansi.runtime.window.WindowService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.skija.Paint
import org.jetbrains.skija.shaper.Shaper
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.lwjgl.system.MemoryStack.stackPush
import java.util.concurrent.TimeUnit

class Engine {
    private val loggingService = Services.create(::LoggingService)
    private val registryService = Services.create(::RegistryService)
    private val resourceService = Services.create(::ResourceService)
    private val windowService = Services.create(::WindowService)
    private val logger by lazy { loggingService.getLogger(Engine::class) }
    private lateinit var windowHandlerRegistration: WindowHandlerRegistration
    private lateinit var mainWindowContext: WindowContext
    private lateinit var gfx: Gfx
    private lateinit var mainRenderTarget: RenderTarget
    private lateinit var defaultPipeline: Pipeline
    private lateinit var uiPipeline: Pipeline
    private lateinit var skijaContext: SkijaContext

    private lateinit var triangleVertices: VertexBuffer
    private lateinit var triangleIndices: IndexBuffer

    private lateinit var orthoProjection: Matrix4fc
    private lateinit var uiQuad: VertexBuffer
    private lateinit var uiQuadIndices: IndexBuffer
    private var frameNo = 0L

    private lateinit var ref: FontImpl
    private lateinit var font: org.jetbrains.skija.Font

    fun run() {
        // load project.cfg
        val projectConfig = loadProjectConfig()
        startServices(projectConfig.logLevel)

        // create main window
        val mainWindow = windowService.createWindow(projectConfig.width, projectConfig.height, projectConfig.title)
        mainWindowContext = WindowContext(mainWindow)

        mainWindow.show()

        gfx = OpenGLGfxDriver.create(mainWindow)
        onFramebufferResize(mainWindow)

        windowHandlerRegistration = mainWindow.registerPlatformStateChangeHandler {
            if (it is PlatformStateChange.FramebufferSize) {
                onFramebufferResize(mainWindow)
            }
        }

        uiQuad = stackPush().use {
            val buffer = it.floats(
                // pos      // tex
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f,

                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f
            )


            gfx.createVertexBuffer(buffer)
        }

//        uiQuadIndices = gfx.createIndexBuffer(stackPush().use {
//            it.ints(
//                0, 1, 2,
//                1, 2, 3
//            )
//        })

        defaultPipeline = gfx.createPipeline {
            vertexAttributes {
                attribute(0, 3, PrimitiveType.FLOAT, 3 * PrimitiveType.FLOAT.size, 0)
            }

            shaderStage(
                ShaderStage.VERTEX, TextShaderSource(
                    """
                    #version 330 core
                    layout (location = 0) in vec3 aPos;
                    
                    void main() {
                        gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
                    }
                    """.trimIndent()
                )
            )

            shaderStage(
                ShaderStage.FRAGMENT, TextShaderSource(
                    """
                    #version 330 core
                    out vec4 FragColor;
    
                    void main() {
                        FragColor = vec4(1.0, 0.5, 0.2, 1.0);
                    } 
                    """.trimIndent()
                )
            )
        }

        uiPipeline = gfx.createPipeline {
            vertexAttributes {
                attribute(0, 2, PrimitiveType.FLOAT, 4 * PrimitiveType.FLOAT.size, 0)
                attribute(
                    1,
                    2,
                    PrimitiveType.FLOAT,
                    4 * PrimitiveType.FLOAT.size,
                    (2 * PrimitiveType.FLOAT.size).toLong()
                )
            }

            shaderStage(
                ShaderStage.VERTEX, TextShaderSource(
                    """
                    #version 330 core
                    layout(location = 0) in vec2 pos;
                    layout(location = 1) in vec2 texCoords;
                    
                    uniform mat4 model;
                    uniform mat4 projection;
                    
                    void main() {
                        gl_Position = projection * model * vec4(pos, 0.0, 1.0);
                    }
                    """.trimIndent()
                )
            )

            shaderStage(
                ShaderStage.FRAGMENT, TextShaderSource(
                    """
                    #version 330 core
                    out vec4 FragColor;
    
                    void main() {
                        FragColor = vec4(0.8, 0.3, 0.2, 1.0);
                    } 
                    """.trimIndent()
                )
            )
        }

        triangleVertices = stackPush().use {
            val buffer = it.floats(
                0.5f, 0.5f, 0.0f,  // top right
                0.5f, -0.5f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f, 0.5f, 0.0f   // top left
            )
            gfx.createVertexBuffer(buffer)
        }

        triangleIndices = stackPush().use {
            val buffer = it.ints(
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
            )
            gfx.createIndexBuffer(buffer)
        }

        ref = resourceService.loadResource("rt://OpenSans-Regular.ttf", Font::class, 0).get() as FontImpl
        font = org.jetbrains.skija.Font(ref.typeface, 32f)

        mainLoop()

        triangleVertices.destroy()
        triangleIndices.destroy()
        defaultPipeline.destroy()
        mainRenderTarget.destroy()
        skijaContext.destroy()

        windowHandlerRegistration.unregister()

        stopServices()
    }

    private fun onFramebufferResize(mainWindow: Window) {
        if (this::mainRenderTarget.isInitialized) {
            mainRenderTarget.destroy()
        }

        val size = mainWindow.getFramebufferSize()

        mainRenderTarget = gfx.createRenderTarget {
            setSize(size.x(), size.y())
            setAttachments(setOf(Attachment.COLOR, Attachment.DEPTH))
        }

        orthoProjection = Matrix4f().ortho(0f, size.x().toFloat(), size.y().toFloat(), 0f, -1f, 1f)

        if (this::skijaContext.isInitialized) {
            skijaContext.destroy()
        }

        skijaContext = SkijaContext.create(
            size.x(),
            size.y(),
            mainRenderTarget
        )
    }

    private fun mainLoop() {
        val orchestrator = Thread {
            var lastTick = millis()
            var deltaMillis: Long

            runCatching {
                EventLoops.Script.scheduleNow {
                    // load default scene
                    val defaultScene =
                        resourceService.loadResource("assets://default.scn.json", Scene::class, 0) as ResourceRef<Scene>
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
                        val size = mainWindowContext.getFramebufferSize()
                        mainRenderTarget.render(defaultPipeline) {
                            setViewport(0, 0, size.x(), size.y())
                            clearColor(0.5f, 0.2f, 0.2f, 1f)

                            setVertices(triangleVertices)
                            setIndices(triangleIndices)

                            drawIndexed(PrimitiveMode.TRIANGLE, 6, 0)
                        }

                        val contentScale = mainWindowContext.getContentScale()
                        skijaContext.draw {
                            resetMatrix()
                            scale(contentScale.x(), contentScale.y())
                            val text = Shaper.make()
                                .shape(
                                    "Hello World",
                                    font,
                                )!!

                            val paint = Paint().setARGB(255, 65, 112, 158)
                            drawTextBlob(
                                text,
                                0f * contentScale.x(),
                                0f * contentScale.y(),
                                paint
                            )
                        }

                        gfx.present(mainRenderTarget)
                        gfx.swap()

                        checkOpenGLError(frameNo++)
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

        resourceService.registerSource(
            "rt",
            RelativeFileSource("rt", JarFileSource(this::class.java.classLoader))
        )

        OpenGLGfxDriver.start()
    }

    private fun stopServices() {
        logger.info { "Stopping services." }
        windowService.stop()
        resourceService.stop()
        registryService.stop()
        loggingService.stop()

        OpenGLGfxDriver.stop()
    }

    private fun millis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadProjectConfig(): ProjectConfig {
        val stream = javaClass.classLoader.getResourceAsStream("project.json") ?: return ProjectConfig()
        return Json.Default.decodeFromStream(stream)
    }
}

fun main() {
    Engine().run()
}