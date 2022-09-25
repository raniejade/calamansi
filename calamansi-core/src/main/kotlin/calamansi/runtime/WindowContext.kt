package calamansi.runtime

import calamansi.event.Event
import calamansi.input.InputContext
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.node.Scene
import calamansi.resource.Resource
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.resource.loaders.SceneLoader
import calamansi.runtime.service.Services
import calamansi.runtime.sys.*
import calamansi.runtime.threading.EventLoops
import calamansi.runtime.utils.FrameStats
import calamansi.ui.*
import calamansi.ui.applyStyle
import calamansi.ui.toPaint
import org.jetbrains.skija.Canvas
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.yoga.Yoga.*
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

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

    private lateinit var renderTarget: RenderTarget
    private lateinit var pipeline: Pipeline
    private lateinit var triangleVertices: VertexBuffer
    private lateinit var triangleIndices: IndexBuffer
    private lateinit var _defaultFont: Font

    inline val defaultFont: Font
        get() = _defaultFont

    val yogaRoot = YGNodeNew()

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

        _defaultFont = loadResource("rt://OpenSans-Regular.ttf", Font::class)
    }

    fun destroy() {
        maybeUnloadCurrentScene()
        eventHandlerRegistration.unregister()
        platformEventHandlerRegistration.unregister()

        renderTarget.destroy()
        pipeline.destroy()
    }

    fun pollEvents() {
        EventLoops.Main.scheduleNow {
            window.pollEvents()
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
            renderTarget.render(pipeline) {
                setViewport(0, 0, size.x(), size.y())
                clearColor(0.5f, 0.2f, 0.2f, 1f)

                setVertices(triangleVertices)
                setIndices(triangleIndices)

                drawIndexed(PrimitiveMode.TRIANGLE, 6, 0)
            }

            val windowSize = window.getWindowSize()
            // TODO: make sure not settable
            canvas.width = FlexValue.Fixed(windowSize.x().toFloat())
            canvas.height = FlexValue.Fixed(windowSize.y().toFloat())
            canvas.applyStyle(yogaRoot)
            layout(node)
            YGNodeCalculateLayout(yogaRoot, YGUndefined, YGUndefined, YGDirectionLTR)
            val contentScale = window.getContentScale()
            renderTarget.draw {
                resetMatrix()
                val paint = canvas.backgroundColor.toPaint()
                drawPaint(paint)
                scale(contentScale.x(), contentScale.y())
                draw(this, node)
            }

            gfx.present(renderTarget)
            gfx.swap()

            checkOpenGLError(frameNo)
            gfx.unbind()
        }
    }

    private fun draw(canvas: Canvas, node: Node?) {
        if (node == null) {
            return
        }

        if (node is CanvasElement) {
            node.layout()
            node.draw(canvas)
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
        if (this::renderTarget.isInitialized) {
            renderTarget.destroy()
        }

        val framebufferSize = window.getFramebufferSize()
        renderTarget = gfx.createRenderTarget {
            setSize(framebufferSize.x(), framebufferSize.y())
            setAttachments(setOf(Attachment.COLOR, Attachment.DEPTH))
        }
        gfx.unbind()

        val windowSize = window.getWindowSize()
        YGNodeStyleSetWidth(yogaRoot, windowSize.x().toFloat())
        YGNodeStyleSetHeight(yogaRoot, windowSize.y().toFloat())
    }

    override val canvas = calamansi.ui.Canvas()

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