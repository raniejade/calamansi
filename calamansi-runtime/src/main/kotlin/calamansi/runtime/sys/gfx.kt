package calamansi.runtime.sys

import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.joml.Vector4fc
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

interface VertexBuffer {
    val sizeInBytes: Long
}

interface IndexBuffer {
    val sizeInBytes: Long
}

sealed interface Texture
interface Texture2d : Texture
enum class TextureFormat {
    RGB,
    RGBA,
    RGB_16F,
    RGBA_16F,
}

sealed class ShaderSource
class TextShaderSource(val source: String) : ShaderSource()
enum class ShaderStage {
    VERTEX,
    FRAGMENT
}

enum class PrimitiveMode {
    TRIANGLE,
    QUAD
}

interface DrawSpec {
    fun setVertices(vertices: VertexBuffer)
    fun setIndices(indices: IndexBuffer)

    fun setViewport(x: Int, y: Int, width: Int, height: Int)

    // vector
    fun setShaderParam(location: Int, resource: Vector3fc)
    fun setShaderParam(location: Int, resource: Array<Vector3fc>)
    fun setShaderParam(location: Int, resource: Vector4fc)
    fun setShaderParam(location: Int, resource: Array<Vector4fc>)

    // matrix
    fun setShaderParam(location: Int, resource: Matrix4fc, transpose: Boolean)
    fun setShaderParam(location: Int, resource: Array<Matrix4fc>, transpose: Boolean)

    // texture
    fun setShaderParam(location: Int, resource: Texture)

    fun clearColor(r: Float, g: Float, b: Float, a: Float)
    fun clearDepth(depth: Float)

    fun draw(mode: PrimitiveMode, count: Int, offset: Long)
    fun draw(mode: PrimitiveMode, count: Int, offset: Long, instances: Int)
}

interface Pipeline
enum class PrimitiveType(val size: Int) {
    FLOAT(Float.SIZE_BYTES),
    UNSIGNED_BYTE(UByte.SIZE_BYTES),
}

interface VertexSpec {
    fun attribute(position: Int, size: Int, type: PrimitiveType, stride: Int, offset: Long)
}

interface PipelineSpec {
    fun vertexAttributes(body: VertexSpec.() -> Unit)
    fun shaderStage(stage: ShaderStage, source: ShaderSource)
}

interface RenderSpec {
    fun Pipeline.use(body: DrawSpec.() -> Unit)
}

interface RenderTarget {
    fun renderToTexture(): Texture2d
    fun render(pipeline: Pipeline, body: DrawSpec.() -> Unit)
}

enum class Attachment {
    COLOR,
    DEPTH
}

interface RenderTargetSpec {
    fun setSize(width: Int, height: Int)
    fun setAttachments(attachments: Set<Attachment>)
}

interface Gfx {
    fun createTexture2d(
        width: Int,
        height: Int,
        sourceFormat: TextureFormat,
        targetFormat: TextureFormat,
        data: ByteBuffer,
    ): Texture2d

    fun createVertexBuffer(data: FloatBuffer): VertexBuffer
    fun createIndexBuffer(data: IntBuffer): IndexBuffer
    fun createRenderTarget(body: RenderTargetSpec.() -> Unit): RenderTarget
    fun createPipeline(body: PipelineSpec.() -> Unit): Pipeline
    fun present(target: RenderTarget)
}

interface GfxDriver {
    fun start()
    fun create(window: Window): Gfx
    fun stop()
}