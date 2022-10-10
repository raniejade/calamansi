package calamansi.runtime

import calamansi.event.Event
import calamansi.gfx.Color
import calamansi.gfx.DefaultRenderSurface
import calamansi.gfx.Font
import calamansi.gfx.RenderSurface
import calamansi.input.InputContext
import calamansi.input.InputEvent
import calamansi.input.InputState
import calamansi.input.MouseButtonStateEvent
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.node.Scene
import calamansi.resource.Resource
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.service.Services
import calamansi.runtime.sys.*
import calamansi.runtime.threading.EventLoops
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
    private lateinit var debugFont: Font
    private var focusedElement: CanvasElement? = null
    private var forceLayout: Boolean = false

    // default render target for this context
    private lateinit var defaultRenderTarget: RenderTarget
    private var surfaceWidth: Float = Float.NaN
    private var surfaceHeight: Float = Float.NaN

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

        // currentTheme = DefaultThemeProvider.create()

        debugFont = resourceService.loadResource("rt://OpenSans-Regular.ttf", Font::class, 0) as Font
    }

    fun destroy() {
        maybeUnloadCurrentScene()
        eventHandlerRegistration.unregister()
        platformEventHandlerRegistration.unregister()

        //_canvas.destroy()
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
            defaultRenderTarget.render(pipeline) {
                setViewport(0, 0, size.x(), size.y())
                clearColor(Color.GRAY)
                setVertices(triangleVertices)
                setIndices(triangleIndices)

                drawIndexed(PrimitiveMode.TRIANGLE, 6, 0)
            }

            // perform layout
            //_canvas.layout()
            //layout(node)

            // calculate layout
            //_canvas.calculateLayout()
            //fetchLayoutValues(node)

            // YGNodeCalculateLayout(yogaRoot, YGUndefined, YGUndefined, YGDirectionLTR)
//            _canvas.renderTarget.draw {
//                resetMatrix()
//                scale(contentScale.x(), contentScale.y())
//                draw(this, node)
//
//                // debug text
//                Shaper.makePrimitive().use { shaper ->
//                    val fps = shaper.shape(
//                        "${"FPS: %d".padEnd(10)} %.2fms".format(frameStats.avgFps.toInt(), frameStats.avgFrameTime),
//                        debugFont.fetchSkijaFont(14f)
//                    )!!
//
//                    drawTextBlob(fps, 5f, 5f, Color.WHITE.toPaint().setStrokeWidth(3f))
//                }
//            }


            // layout pass
            layout2(node, forceLayout)
            forceLayout = false

            // draw canvas in batch
            val contentScale = window.getContentScale()
            val drawBatch = findSurfaces(node)
            for (batch in drawBatch) {
                val renderTarget = getRenderTarget(batch.key)
                renderTarget.draw {
                    resetMatrix()
                    scale(contentScale.x(), contentScale.y())
                    for (c in batch.value) {
                        draw2(this, node)
                    }

                    if (batch.key is DefaultRenderSurface) {
                        // debug
                        Shaper.makePrimitive().use { shaper ->
                            val fps = shaper.shape(
                                "${"FPS: %d".padEnd(10)} %.2fms".format(
                                    frameStats.avgFps.toInt(),
                                    frameStats.avgFrameTime
                                ),
                                debugFont.fetchSkijaFont(14f)
                            )!!

                            drawTextBlob(fps, 5f, 5f, Color.WHITE.toPaint().setStrokeWidth(3f))
                        }
                    }
                }
            }

            gfx.present(defaultRenderTarget)
            gfx.swap()

            checkOpenGLError(frameNo)
            gfx.unbind()
        }
    }

    fun requestFocus(element: CanvasElement?) {
        focusedElement?.publishUnFocusMessage()
        focusedElement = element
        focusedElement?.publishFocusMessage()
    }

    // returns true if pressed element is not the currently focused element
    fun shouldLoseFocus(pressedElement: CanvasElement): Boolean {
        return !isFocused(pressedElement)
    }

    fun isFocused(element: CanvasElement): Boolean {
        return focusedElement === element
    }

    private fun getRenderTarget(surface: RenderSurface): RenderTarget {
        return when (surface) {
            is DefaultRenderSurface -> defaultRenderTarget
            else -> throw AssertionError("Unsupported surface: $surface ")
        }
    }

    private fun findSurfaces(node: Node?): Map<RenderSurface, List<calamansi.ui2.control.Canvas>> {
        val builder = mutableMapOf<RenderSurface, MutableList<calamansi.ui2.control.Canvas>>()
        findSurfaces(node, builder)
        return builder.mapValues { it.value.toList() }
    }

    private fun findSurfaces(
        node: Node?,
        builder: MutableMap<RenderSurface, MutableList<calamansi.ui2.control.Canvas>>
    ) {
        if (node == null) {
            return
        }

        if (node is calamansi.ui2.control.Canvas) {
            val list = builder.getOrPut(node.surface) { mutableListOf() }
            list.add(node)
        }

        for (child in node.getChildren()) {
            findSurfaces(child, builder)
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

    private fun fetchLayoutValues(node: Node?) {
        if (node == null) {
            return
        }

        if (node is CanvasElement) {
            node.fetchLayoutValues()
        }

        for (child in node.getChildren()) {
            fetchLayoutValues(child)
        }
    }

    private fun layout2(node: Node?, forceLayout: Boolean) {
        if (node == null) {
            return
        }

        if (node is calamansi.ui2.control.Canvas) {
            if (node.surface is DefaultRenderSurface) {
                node.layout(forceLayout, surfaceWidth, surfaceHeight)
            } else {
                logger.error("Unsupported surface: ${node.surface}, ignoring.")
            }
        }

        for (child in node.getChildren()) {
            layout2(child, forceLayout)
        }
    }

    private fun draw2(skijaCanvas: SkijaCanvas, node: Node?) {
        if (node == null) {
            return
        }

        if (node is calamansi.ui2.control.Canvas) {
            node.draw(skijaCanvas)

        }

        for (child in node.getChildren()) {
            draw2(skijaCanvas, child)
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

        if (event is InputEvent) {
            node?.invokeOnGuiEvent(event)
            if (event.isConsumed()) {
                return
            }
        }

        node?.invokeOnUnhandledEvent(event)

        if (event.isConsumed()) {
            return
        }

        if (event is MouseButtonStateEvent && event.state == InputState.PRESSED) {
            requestFocus(null)
        }
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
        if (this::defaultRenderTarget.isInitialized) {
            this.defaultRenderTarget.destroy()
        }
        this.defaultRenderTarget = renderTarget
        surfaceWidth = windowSize.x().toFloat()
        surfaceHeight = windowSize.y().toFloat()
        forceLayout = true
        gfx.unbind()
    }

    override fun setTheme(theme: calamansi.ui2.control.Theme) {
        calamansi.ui2.control.Theme.setCurrent(theme)
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