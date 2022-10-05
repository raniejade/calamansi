package calamansi.runtime

import calamansi.event.Event
import calamansi.gfx.Color
import calamansi.input.InputContext
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.node.Scene
import calamansi.resource.Resource
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.service.Services
import calamansi.runtime.sys.*
import calamansi.runtime.threading.EventLoops
import calamansi.runtime.ui.DefaultThemeProvider
import calamansi.runtime.utils.FrameStats
import calamansi.ui.*
import io.github.humbleui.skija.shaper.Shaper
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import io.github.humbleui.skija.Canvas as SkijaCanvas

internal class WindowContext(
    private val window: Window,
    private val gfx: Gfx,
    private val frameStats: FrameStats,
) : ExecutionContext,
    InputContext by window {
    private val logger = LoggerFactory.getLogger(WindowContext::class.java)
    private var node: Node? = null
    private val eventHandlerRegistration: WindowHandlerRegistration = window.registerEventHandler(this::handleEvent)
    private val platformEventHandlerRegistration: WindowHandlerRegistration =
        window.registerPlatformStateChangeHandler(this::handlePlatformStateChange)
    private val resourceService: ResourceService by Services.get()

    private val _canvas: calamansi.ui.Canvas = Canvas()
    private lateinit var pipeline: Pipeline
    private lateinit var triangleVertices: VertexBuffer
    private lateinit var triangleIndices: IndexBuffer
    private lateinit var currentTheme: Theme
    private lateinit var debugFont: Font

    fun init() {
        framebufferResized()

        gfx.bind()
        pipeline = gfx.createPipeline {
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

        triangleVertices = MemoryStack.stackPush().use {
            val buffer = it.floats(
                0.5f, 0.5f, 0.0f,  // top right
                0.5f, -0.5f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f, 0.5f, 0.0f   // top left
            )
            gfx.createVertexBuffer(buffer)
        }

        triangleIndices = MemoryStack.stackPush().use {
            val buffer = it.ints(
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
            )
            gfx.createIndexBuffer(buffer)
        }

        gfx.unbind()

        currentTheme = DefaultThemeProvider.create()

        debugFont = resourceService.loadResource("rt://OpenSans-Regular.ttf", Font::class, 0) as Font
    }

    fun destroy() {
        maybeUnloadCurrentScene()
        eventHandlerRegistration.unregister()
        platformEventHandlerRegistration.unregister()

        _canvas.destroy()
        pipeline.destroy()
    }

    fun pollEvents() {
        EventLoops.Main.scheduleNow {
            window.pollEvents()
        }

        EventLoops.Script.scheduleNow {
            window.processEvents()
        }
    }

    fun frame(delta: Float, frameNo: Long) {
        EventLoops.Script.scheduleNow {
            node?.invokeOnUpdate(delta)
        }
    }

    fun render(frameNo: Long) {
        EventLoops.Main.scheduleNow {
            gfx.bind()
            val size = window.getFramebufferSize()
            // don't use canvas.clear/paint as it is slow!
            _canvas.renderTarget.render(pipeline) {
                setViewport(0, 0, size.x(), size.y())
                clearColor(Color.GRAY)
                setVertices(triangleVertices)
                setIndices(triangleIndices)

                drawIndexed(PrimitiveMode.TRIANGLE, 6, 0)
            }

            // perform layout
            _canvas.layout()
            layout(node)

            // calculate layout
            _canvas.calculateLayout()

            // YGNodeCalculateLayout(yogaRoot, YGUndefined, YGUndefined, YGDirectionLTR)
            val contentScale = window.getContentScale()
            _canvas.renderTarget.draw {
                resetMatrix()
                scale(contentScale.x(), contentScale.y())
                draw(this, node)

                // debug text
                Shaper.makePrimitive().use { shaper ->
                    val fps = shaper.shape("${"FPS: %d".padEnd(10)} %.2fms".format(frameStats.avgFps.toInt(), frameStats.avgFrameTime),
                        debugFont.fetchSkijaFont(14f))!!

                    drawTextBlob(fps, 5f, 5f, Color.WHITE.toPaint().setStrokeWidth(3f))
                }
            }

            gfx.present(_canvas.renderTarget)
            gfx.swap()

            checkOpenGLError(frameNo)
            gfx.unbind()
        }
    }

    private fun draw(canvas: SkijaCanvas, node: Node?) {
        if (node == null) {
            return
        }

        if (node is CanvasElement) {
            node.layout()
            canvas.save()
            val pos = node.getLayoutPos()
            canvas.translate(pos.x(), pos.y())
            node.draw(canvas)
            canvas.restore()
        }

        for (child in node.getChildren()) {
            draw(canvas, child)
        }
    }

    private fun layout(node: Node?) {
        if (node == null) {
            return
        }

        if (node is CanvasElement) {
            node.layout()
        }

        for (child in node.getChildren()) {
            layout(child)
        }
    }

    fun shouldCloseWindow() = window.shouldCloseWindow()

    private fun handleEvent(event: Event) {
        if (node == null) {
            return
        }

        node?.invokeOnEvent(event)

        if (event.isConsumed()) {
            return
        }

        node?.invokeOnUnhandledEvent(event)
    }

    private fun handlePlatformStateChange(stateChange: PlatformStateChange) {
        if (stateChange is PlatformStateChange.FramebufferSize) {
            framebufferResized()
        }
    }

    private fun checkOpenGLError(frameCount: Long) {
        var error: Int
        do {
            error = GL30.glGetError()

            if (error != GL30.GL_NO_ERROR) {
                logger.warn("[frame: $frameCount]: opengl error ${error.toString(16)}")
            }

        } while (error != GL30.GL_NO_ERROR)
    }

    private fun framebufferResized() {
        gfx.bind()
        val windowSize = window.getWindowSize()
        val framebufferSize = window.getFramebufferSize()
        val renderTarget = gfx.createRenderTarget {
            setSize(framebufferSize.x(), framebufferSize.y())
            setAttachments(setOf(Attachment.COLOR, Attachment.DEPTH))
        }
        _canvas.configure(windowSize.x(), windowSize.y(), renderTarget)
        gfx.unbind()
    }

    override inline val canvas: calamansi.ui.Canvas
        get() = _canvas

    override fun getFrameTime(): Float {
        return frameStats.avgFrameTime
    }

    override fun getFps(): Float {
        return frameStats.avgFps
    }

    override fun setScene(scene: Scene) {
        maybeUnloadCurrentScene()
        node = scene.instance()
        node?.let {
            it.executionContext = this
            it.theme = currentTheme
            it.invokeOnEnterTree()
        }
    }

    override fun exit() {
        window.closeWindow()
    }

    override fun setCursor(cursor: Cursor) {
        EventLoops.Main.scheduleNow {
            window.setCursor(cursor)
        }
    }

    override fun <T : Resource> loadResourceAsync(
        path: String,
        type: KClass<T>,
        index: Int
    ): CompletableFuture<T> {
        return EventLoops.Resource.schedule {
            resourceService.loadResource(path, type, index) as T
        }
    }

    private fun maybeUnloadCurrentScene() {
        val oldRoot = node
        // unload previous scene
        if (oldRoot != null) {
            oldRoot.invokeOnExitTree()
            oldRoot.executionContext = null
            node = null
        }
    }
}