package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.PrimitiveType
import calamansi.runtime.sys.VertexSpec
import org.lwjgl.opengl.GL30.glEnableVertexAttribArray
import org.lwjgl.opengl.GL30.glVertexAttribPointer

class VertexSpecImpl: VertexSpec {
    override fun attribute(position: Int, size: Int, type: PrimitiveType, stride: Int, offset: Long) {
        glVertexAttribPointer(position, size, type.toGL(), false, stride, offset)
        glEnableVertexAttribArray(position)
    }
}