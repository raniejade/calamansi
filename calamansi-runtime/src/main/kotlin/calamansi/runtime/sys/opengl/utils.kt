package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.PrimitiveMode
import calamansi.runtime.sys.PrimitiveType
import calamansi.runtime.sys.TextureFormat
import org.lwjgl.opengl.GL30.*

fun TextureFormat.toGL(): Int {
    return when (this) {
        TextureFormat.RGB -> GL_RGB
        TextureFormat.RGBA -> GL_RGBA
        TextureFormat.RGB_16F -> GL_RGB16
        TextureFormat.RGBA_16F -> GL_RGBA16
    }
}

fun PrimitiveMode.toGL(): Int {
    return when (this) {
        PrimitiveMode.TRIANGLE -> GL_TRIANGLES
        PrimitiveMode.QUAD -> GL_QUADS
        PrimitiveMode.TRIANGLE_STRIP -> GL_TRIANGLE_STRIP
    }
}

fun PrimitiveType.toGL(): Int {
    return when (this) {
        PrimitiveType.FLOAT -> GL_FLOAT
        PrimitiveType.UNSIGNED_BYTE -> GL_UNSIGNED_BYTE
    }
}

fun checkOpenGLError(frameNo: Long) {
    val error = glGetError()
    check(error == GL_NO_ERROR) {
        "[frame: $frameNo]: opengl error ${error.toString(16)}"
    }
}