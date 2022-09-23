package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.ShaderSource
import calamansi.runtime.sys.TextShaderSource
import org.lwjgl.opengl.GL30

internal object ShaderUtils {
    fun createProgram(shaders: List<Int>): Int {
        val program = GL30.glCreateProgram()
        shaders.forEach { shader ->
            GL30.glAttachShader(program, shader)
        }
        GL30.glLinkProgram(program)
        val success = IntArray(1)
        GL30.glGetProgramiv(program, GL30.GL_LINK_STATUS, success)
        check(success[0] == GL30.GL_TRUE) {
            GL30.glGetProgramInfoLog(program)
        }

        // delete after linking
        shaders.forEach { shader ->
            GL30.glDeleteShader(shader)
        }
        return program
    }

    fun createShader(type: Int, source: ShaderSource): Int {
        val shader = GL30.glCreateShader(type)
        when (source) {
            is TextShaderSource -> {
                GL30.glShaderSource(shader, source.source)
                GL30.glCompileShader(shader)
            }
        }
        val success = IntArray(1)
        GL30.glGetShaderiv(shader, GL30.GL_COMPILE_STATUS, success)
        check(value = success[0] == GL30.GL_TRUE) {
            GL30.glGetShaderInfoLog(shader)
        }
        return shader
    }
}