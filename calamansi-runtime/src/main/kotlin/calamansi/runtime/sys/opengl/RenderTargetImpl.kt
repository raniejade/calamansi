package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.DrawSpec
import calamansi.runtime.sys.Pipeline
import calamansi.runtime.sys.RenderTarget
import calamansi.runtime.sys.Texture2d
import org.lwjgl.opengl.ARBFramebufferObject.*
import org.lwjgl.opengl.GL20.glDeleteTextures
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL30.glBindVertexArray

class Framebuffer(val handle: Int, val color: Int, val depth: Int?)
class RenderTargetImpl(
    val front: Framebuffer,
) : RenderTarget {
    override fun renderToTexture(): Texture2d {
        return Texture2dImpl(front.color)
    }

    override fun render(pipeline: Pipeline, body: DrawSpec.() -> Unit) {
        check(pipeline is PipelineImpl)
        glBindFramebuffer(GL_FRAMEBUFFER, front.handle)

        glUseProgram(pipeline.program)
        glBindVertexArray(pipeline.handle)

        DrawSpecImpl(pipeline).body()

        glUseProgram(0)
        glBindVertexArray(0)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    override fun destroy() {
        glDeleteFramebuffers(front.handle)
        glDeleteTextures(front.color)
        front.depth?.let { glDeleteTextures(it) }
    }
}