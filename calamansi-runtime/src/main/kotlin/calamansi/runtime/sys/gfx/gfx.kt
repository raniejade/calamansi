package calamansi.runtime.sys.gfx

import calamansi.runtime.sys.window.Window
import org.joml.Matrix4f
import java.nio.FloatBuffer
import java.nio.IntBuffer

interface Surface {
    fun destroy()
}

enum class VertexAttribute {
    POSITION,
    NORMAL,
    TEXTURE_COORDINATES,
}

enum class DrawMode {
    TRIANGLE,
    QUAD,
}

interface Draw {
    fun setProjectionMatrix(projection: Matrix4f)
    fun setViewMatrix(view: Matrix4f)

    fun uploadVertexBufferData(vertices: FloatBuffer, attributes: List<VertexAttribute>)
    fun uploadIndexBufferData(indices: IntBuffer)

    fun draw(mode: DrawMode)
    fun drawInstanced(mode: DrawMode, count: Int)
}

interface Gfx {
    fun createSurface(window: Window): Surface
    fun startDraw(surface: Surface): Draw
}

interface GfxDriver {
    fun init()
    fun create(): Gfx
    fun shutdown()
}