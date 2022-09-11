package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.*
import calamansi.runtime.sys.opengl.ShaderUtils.createProgram
import calamansi.runtime.sys.opengl.ShaderUtils.createShader
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL30.*

class PipelineSpecImpl : PipelineSpec {
    private lateinit var configureVertexAttributes: (VertexSpec.() -> Unit)
    private val shaderStages = mutableMapOf<ShaderStage, ShaderSource>()

    override fun vertexAttributes(body: VertexSpec.() -> Unit) {
        this.configureVertexAttributes = body
    }

    override fun shaderStage(stage: ShaderStage, source: ShaderSource) {
        check(!shaderStages.containsKey(stage)) { "Source for $stage is already defined." }
        shaderStages[stage] = source
    }

    fun build(): Pipeline {
        val vertexShaderSource = checkNotNull(shaderStages[ShaderStage.VERTEX]) { "Vertex shader not specified." }
        val fragmentShaderSource = checkNotNull(shaderStages[ShaderStage.FRAGMENT]) { "Fragment shader not specified." }
        val vao = glGenVertexArrays()
        glBindVertexArray(vao)
        // 50mb vertex buffer (TODO: make configurable)
        val vertexBuffer = VertexBufferImpl(glGenBuffers(), 50 * 1024)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.handle)
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.sizeInBytes, GL_DYNAMIC_DRAW)
        VertexSpecImpl().configureVertexAttributes()
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        // 25mb index buffer (TODO: make configurable)
        val indexBuffer = IndexBufferImpl(glGenBuffers(), 25 * 1024)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.handle)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.sizeInBytes, GL_DYNAMIC_DRAW)

        glBindVertexArray(0)
        // must happen after unbinding vao, otherwise the unbinding is remembered by it.
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        val shaders = listOf(
            createShader(GL_VERTEX_SHADER, vertexShaderSource),
            createShader(GL_FRAGMENT_SHADER, fragmentShaderSource),
        )
        val program = createProgram(shaders)
        return PipelineImpl(vao, program, vertexBuffer, indexBuffer)
    }
}