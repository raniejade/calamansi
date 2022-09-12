package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.*
import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.joml.Vector4fc
import org.lwjgl.opengl.ARBSeparateShaderObjects.*
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL31.glDrawElementsInstanced
import org.lwjgl.system.MemoryStack.stackPush
import java.nio.FloatBuffer
import java.nio.IntBuffer

class DrawSpecImpl(private val pipeline: PipelineImpl) : DrawSpec {
    override fun setVertices(vertices: VertexBuffer) {
        pipeline.copyVertices(vertices as VertexBufferImpl)
    }

    override fun setIndices(indices: IndexBuffer) {
        pipeline.copyIndices(indices as IndexBufferImpl)
    }

    override fun setViewport(x: Int, y: Int, width: Int, height: Int) {
        glViewport(x, y, width, height)
    }

    override fun setShaderParam(location: Int, resource: Vector3fc) {
        glProgramUniform3f(pipeline.program, location, resource.x(), resource.y(), resource.z())
    }

    override fun setShaderParam(location: Int, resource: Array<Vector3fc>) {
        stackPush().use { stack ->
            val buffer = stack.mallocFloat(3 * resource.size)
            for (i in resource.indices) {
                val element = resource[i]
                buffer.put(element.x())
                buffer.put(element.y())
                buffer.put(element.z())
            }
            buffer.reset()
            glProgramUniform3fv(pipeline.program, location, buffer)
        }
    }

    override fun setShaderParam(location: Int, resource: Vector4fc) {
        glProgramUniform4f(pipeline.program, location, resource.x(), resource.y(), resource.z(), resource.w())
    }

    override fun setShaderParam(location: Int, resource: Array<Vector4fc>) {
        stackPush().use { stack ->
            val buffer = stack.mallocFloat(4 * resource.size)
            for (i in resource.indices) {
                val element = resource[i]
                buffer.put(element.x())
                buffer.put(element.y())
                buffer.put(element.z())
                buffer.put(element.w())
            }
            buffer.reset()
            glProgramUniform4fv(pipeline.program, location, buffer)
        }
    }

    override fun setShaderParam(location: Int, resource: Matrix4fc, transpose: Boolean) {
        stackPush().use { stack ->
            val buffer = resource.get(stack.mallocFloat(16))
            buffer.reset()
            glProgramUniform4fv(pipeline.program, location, buffer)
        }
    }

    override fun setShaderParam(location: Int, resource: Array<Matrix4fc>, transpose: Boolean) {
        stackPush().use { stack ->
            val buffer = stack.mallocFloat(16 * resource.size)
            resource.forEach { matrix ->
                matrix.get(buffer)
            }
            buffer.reset()
            glProgramUniform4fv(pipeline.program, location, buffer)
        }
    }

    override fun setShaderParam(location: Int, resource: Texture) {
        glActiveTexture(GL_TEXTURE0 + location)
        when (resource) {
            is Texture2d -> {
                glBindTexture(GL_TEXTURE_2D, (resource as Texture2dImpl).handle)
            }
        }
    }

    override fun clearColor(r: Float, g: Float, b: Float, a: Float) {
        glClearColor(r, g, b, a)
        glClear(GL_COLOR_BUFFER_BIT)
    }

    override fun clearDepth(depth: Float) {
        glClearDepth(depth.toDouble())
        glClear(GL_DEPTH_BUFFER_BIT)
    }

    override fun draw(mode: PrimitiveMode, count: Int, offset: Long) {
        glDrawElements(mode.toGL(), count, GL_UNSIGNED_INT, offset)
    }

    override fun draw(mode: PrimitiveMode, count: Int, offset: Long, instances: Int) {
        glDrawElementsInstanced(mode.toGL(), count, GL_UNSIGNED_INT, offset, instances)
    }
}