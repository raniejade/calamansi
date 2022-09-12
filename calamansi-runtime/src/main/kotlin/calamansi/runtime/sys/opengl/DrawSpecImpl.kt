package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.*
import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.joml.Vector4fc
import org.lwjgl.opengl.ARBSeparateShaderObjects.*
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL31.*
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

    override fun setShaderParam(name: String, resource: Vector3fc) {
        glProgramUniform3f(pipeline.program, getUniformLocation(name), resource.x(), resource.y(), resource.z())
    }

    override fun setShaderParam(name: String, resource: Array<Vector3fc>) {
        stackPush().use { stack ->
            val buffer = stack.mallocFloat(3 * resource.size)
            for (i in resource.indices) {
                val element = resource[i]
                buffer.put(element.x())
                buffer.put(element.y())
                buffer.put(element.z())
            }
            glProgramUniform3fv(pipeline.program, getUniformLocation(name), buffer)
        }
    }

    override fun setShaderParam(name: String, resource: Vector4fc) {
        glProgramUniform4f(
            pipeline.program,
            getUniformLocation(name),
            resource.x(),
            resource.y(),
            resource.z(),
            resource.w()
        )
    }

    override fun setShaderParam(name: String, resource: Array<Vector4fc>) {
        stackPush().use { stack ->
            val buffer = stack.mallocFloat(4 * resource.size)
            for (i in resource.indices) {
                val element = resource[i]
                buffer.put(element.x())
                buffer.put(element.y())
                buffer.put(element.z())
                buffer.put(element.w())
            }
            glProgramUniform4fv(pipeline.program, getUniformLocation(name), buffer)
        }
    }

    override fun setShaderParam(name: String, resource: Matrix4fc, transpose: Boolean) {
        stackPush().use { stack ->
            val buffer = resource.get(stack.mallocFloat(16))
            glProgramUniformMatrix4fv(pipeline.program, getUniformLocation(name), transpose, buffer)
        }
    }

    override fun setShaderParam(name: String, resource: Array<Matrix4fc>, transpose: Boolean) {
        stackPush().use { stack ->
            val buffer = stack.mallocFloat(16 * resource.size)
            resource.forEach { matrix ->
                matrix.get(buffer)
            }
            glProgramUniformMatrix4fv(pipeline.program, getUniformLocation(name), transpose, buffer)
        }
    }

    override fun setShaderParam(name: String, resource: Texture) {
        glActiveTexture(GL_TEXTURE0 + getUniformLocation(name))
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

    override fun draw(mode: PrimitiveMode, count: Int) {
        glDrawArrays(mode.toGL(), 0, count)
    }

    override fun drawInstanced(mode: PrimitiveMode, count: Int, instances: Int) {
        glDrawArraysInstanced(mode.toGL(), 0, count, instances)
    }

    override fun drawIndexed(mode: PrimitiveMode, count: Int, offset: Long) {
        glDrawElements(mode.toGL(), count, GL_UNSIGNED_INT, offset)
    }

    override fun drawIndexedInstanced(mode: PrimitiveMode, count: Int, offset: Long, instances: Int) {
        glDrawElementsInstanced(mode.toGL(), count, GL_UNSIGNED_INT, offset, instances)
    }

    private fun getUniformLocation(name: String): Int {
        return glGetUniformLocation(pipeline.program, name)
    }
}