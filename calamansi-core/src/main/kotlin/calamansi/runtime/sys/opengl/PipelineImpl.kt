package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.Pipeline
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL31.*

internal class PipelineImpl(
    val handle: Int,
    val program: Int,
    private var vertexBuffer: VertexBufferImpl,
    private var indexBuffer: IndexBufferImpl,
) : Pipeline {
    fun copyVertices(vertices: VertexBufferImpl) {
        maybeResizeVertexBuffer(vertexBuffer.handle, vertexBuffer.sizeInBytes, vertices.sizeInBytes)
        copyBuffer(vertices.handle, vertexBuffer.handle, vertices.sizeInBytes)

    }

    fun copyIndices(indices: IndexBufferImpl) {
        maybeResizeVertexBuffer(indexBuffer.handle, indexBuffer.sizeInBytes, indices.sizeInBytes)
        copyBuffer(indices.handle, indexBuffer.handle, indices.sizeInBytes)
    }

    private fun copyBuffer(source: Int, target: Int, size: Long) {
        glBindBuffer(GL_COPY_READ_BUFFER, source)
        glBindBuffer(GL_COPY_WRITE_BUFFER, target)

        glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, size)

        glBindBuffer(GL_COPY_READ_BUFFER, 0)
        glBindBuffer(GL_COPY_WRITE_BUFFER, 0)
    }


    private fun maybeResizeVertexBuffer(buffer: Int, currentSize: Long, requiredSize: Long) {
        if (currentSize >= requiredSize) {
            // no resize required
            return
        }
        glBindBuffer(GL_COPY_WRITE_BUFFER, buffer)
        glBufferData(GL_COPY_WRITE_BUFFER, requiredSize, GL_DYNAMIC_DRAW)
        glBindBuffer(GL_COPY_WRITE_BUFFER, 0)
    }

    override fun destroy() {
        GL30.glDeleteVertexArrays(handle)
        glDeleteProgram(program)
        vertexBuffer.destroy()
        indexBuffer.destroy()
    }
}