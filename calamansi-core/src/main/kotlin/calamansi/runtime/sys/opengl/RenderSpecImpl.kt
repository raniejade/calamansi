package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.DrawSpec
import calamansi.runtime.sys.Pipeline
import calamansi.runtime.sys.RenderSpec
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL30.glBindVertexArray

internal class RenderSpecImpl : RenderSpec {
    override fun Pipeline.use(body: DrawSpec.() -> Unit) {
        check(this is PipelineImpl)
        glUseProgram(program)
        glBindVertexArray(handle)

        DrawSpecImpl(this).body()

        glUseProgram(0)
        glBindVertexArray(0)
    }
}