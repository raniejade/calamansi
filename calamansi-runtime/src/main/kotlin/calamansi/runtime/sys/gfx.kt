package calamansi.runtime.sys

import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.joml.Vector4fc
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

interface VertexBuffer {
    val sizeInBytes: Long
    fun destroy()
}

interface IndexBuffer {
    val sizeInBytes: Long
    fun destroy()
}

sealed interface Texture {
    fun destroy()
}
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
    QUAD,
    TRIANGLE_STRIP,
}

interface DrawSpec {
    fun setVertices(vertices: VertexBuffer)
    fun setIndices(indices: IndexBuffer)

    fun setViewport(x: Int, y: Int, width: Int, height: Int)
    fun setEnableDepthTest(enable: Boolean)

    // vector
    fun setShaderParam(name: String, resource: Vector3fc)
    fun setShaderParam(name: String, resource: Array<Vector3fc>)
    fun setShaderParam(name: String, resource: Vector4fc)
    fun setShaderParam(name: String, resource: Array<Vector4fc>)

    // matrix
    fun setShaderParam(name: String, resource: Matrix4fc, transpose: Boolean)
    fun setShaderParam(name: String, resource: Array<Matrix4fc>, transpose: Boolean)

    // texture
    fun setShaderParam(name: String, resource: Texture)

    fun clearColor(r: Float, g: Float, b: Float, a: Float)
    fun clearDepth(depth: Float)

    fun draw(mode: PrimitiveMode, count: Int)
    fun drawInstanced(mode: PrimitiveMode, count: Int, instances: Int)
    fun drawIndexed(mode: PrimitiveMode, count: Int, offset: Long)
    fun drawIndexedInstanced(mode: PrimitiveMode, count: Int, offset: Long, instances: Int)
}

interface Pipeline {
    fun destroy()
}
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
    fun destroy()
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
    fun swap()
}

interface GfxDriver {
    fun start()
    fun create(window: Window): Gfx
    fun stop()
}