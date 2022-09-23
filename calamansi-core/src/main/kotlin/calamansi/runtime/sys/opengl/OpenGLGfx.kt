package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.*
import calamansi.runtime.sys.glfw.GlfwWindow
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.glDeleteTextures
import org.lwjgl.opengl.GL15.glDeleteBuffers
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GLCapabilities
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class VertexBufferImpl(val handle: Int, override val sizeInBytes: Long) : VertexBuffer {
    override fun destroy() {
        glDeleteBuffers(handle)
    }
}

class IndexBufferImpl(val handle: Int, override val sizeInBytes: Long) : IndexBuffer {
    override fun destroy() {
        glDeleteBuffers(handle)
    }
}

class Texture2dImpl(val handle: Int) : Texture2d {
    override fun destroy() {
        glDeleteTextures(handle)
    }
}

internal class OpenGLGfx(val window: GlfwWindow, val capabilities: GLCapabilities) : Gfx {
    private val presentationQuadData = floatArrayOf(
        // positions   // texCoords
        -1.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, 1.0f, 0.0f,

        -1.0f, 1.0f, 0.0f, 1.0f,
        1.0f, -1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f,
    )
    private val presentationVbo by lazy {
        glGenBuffers()
    }

    private val presentationVao by lazy {
        val vao = glGenVertexArrays()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, presentationVbo)
        glBufferData(GL_ARRAY_BUFFER, presentationQuadData, GL_STATIC_DRAW)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0L)
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        vao
    }

    private val presentationProgram by lazy {
        val vs = ShaderUtils.createShader(
            GL_VERTEX_SHADER, TextShaderSource(
                """
                #version 330 core
                layout (location = 0) in vec2 aPos;
                layout (location = 1) in vec2 aTexCoords;

                out vec2 TexCoords;

                void main() {
                    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0); 
                    TexCoords = aTexCoords;
                }
            """.trimIndent()
            )
        )

        val fs = ShaderUtils.createShader(
            GL_FRAGMENT_SHADER, TextShaderSource(
                """
                #version 330 core
                out vec4 FragColor;
                  
                in vec2 TexCoords;

                uniform sampler2D screenTexture;

                void main() { 
                    FragColor = texture(screenTexture, TexCoords);
                }
            """.trimIndent()
            )
        )

        ShaderUtils.createProgram(listOf(vs, fs))
    }

    override fun bind() {
        glfwMakeContextCurrent(window.handle)
        GL.setCapabilities(capabilities)
    }

    override fun unbind() {
        glfwMakeContextCurrent(0)
    }

    override fun createTexture2d(
        width: Int,
        height: Int,
        sourceFormat: TextureFormat,
        targetFormat: TextureFormat,
        data: ByteBuffer,
    ): Texture2d {
        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            targetFormat.toGL(),
            width,
            height,
            0,
            sourceFormat.toGL(),
            GL_UNSIGNED_BYTE,
            data
        )
        glGenerateMipmap(GL_TEXTURE_2D)
        return Texture2dImpl(texture)
    }

    override fun createVertexBuffer(data: FloatBuffer): VertexBuffer {
        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        // TODO: make immutable (only possible in opengl 4.5)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        return VertexBufferImpl(vbo, Integer.toUnsignedLong(data.remaining()) shl 2)
    }

    override fun createIndexBuffer(data: IntBuffer): IndexBuffer {
        val ebo = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        // TODO: make immutable (only possible in opengl 4.5)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        return IndexBufferImpl(ebo, Integer.toUnsignedLong(data.remaining()) shl 2)
    }

    override fun createRenderTarget(body: RenderTargetSpec.() -> Unit): RenderTarget {
        val spec = RenderTargetSpecImpl()
        spec.body()
        return spec.build()
    }

    override fun createPipeline(body: PipelineSpec.() -> Unit): Pipeline {
        val spec = PipelineSpecImpl()
        spec.body()
        return spec.build()
    }

    override fun present(target: RenderTarget) {
        check(target is RenderTargetImpl)
        // bind default
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glDisable(GL_DEPTH_TEST)

        glUseProgram(presentationProgram)
        glBindVertexArray(presentationVao)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, (target.renderToTexture() as Texture2dImpl).handle)
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindTexture(GL_TEXTURE_2D, 0)

        glUseProgram(0)
        glBindVertexArray(0)
    }

    override fun swap() {
        if (window is GlfwWindow) {
            window.swapBuffers()
        }
    }
}