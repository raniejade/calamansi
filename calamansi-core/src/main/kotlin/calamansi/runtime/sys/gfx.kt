package calamansi.runtime.sys

import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.joml.Vector4fc
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

internal interface VertexBuffer {
    val sizeInBytes: Long
    fun destroy()
}

internal interface IndexBuffer {
    val sizeInBytes: Long
    fun destroy()
}

internal sealed interface Texture {
    fun destroy()
}

internal interface Texture2d : Texture
internal enum class TextureFormat {
    RGB,
    RGBA,
    RGB_16F,
    RGBA_16F,
}

internal sealed class ShaderSource
internal class TextShaderSource(val source: String) : ShaderSource()
internal enum class ShaderStage {
    VERTEX,
    FRAGMENT
}

internal enum class PrimitiveMode {
    TRIANGLE,
    QUAD,
    TRIANGLE_STRIP,
}

internal interface DrawSpec {
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

internal interface Pipeline {
    fun destroy()
}

internal enum class PrimitiveType(val size: Int) {
    FLOAT(Float.SIZE_BYTES),
    UNSIGNED_BYTE(UByte.SIZE_BYTES),
}

internal interface VertexSpec {
    fun attribute(position: Int, size: Int, type: PrimitiveType, stride: Int, offset: Long)
}

internal interface PipelineSpec {
    fun vertexAttributes(body: VertexSpec.() -> Unit)
    fun shaderStage(stage: ShaderStage, source: ShaderSource)
}

internal interface RenderSpec {
    fun Pipeline.use(body: DrawSpec.() -> Unit)
}

internal interface RenderTarget {
    fun renderToTexture(): Texture2d
    fun render(pipeline: Pipeline, body: DrawSpec.() -> Unit)
    fun destroy()
}

internal enum class Attachment {
    COLOR,
    DEPTH
}

internal interface RenderTargetSpec {
    fun setSize(width: Int, height: Int)
    fun setAttachments(attachments: Set<Attachment>)
}

internal interface Gfx {
    fun bind()
    fun unbind()
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

internal interface GfxDriver {
    fun start()
    fun create(window: Window): Gfx
    fun stop()
}