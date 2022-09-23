package calamansi.runtime.sys.opengl

import calamansi.runtime.sys.Attachment
import calamansi.runtime.sys.RenderTarget
import calamansi.runtime.sys.RenderTargetSpec
import calamansi.runtime.sys.TextureFormat
import org.lwjgl.opengl.ARBFramebufferObject.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL

internal class RenderTargetSpecImpl : RenderTargetSpec {
    private data class ColorAttachment(
        val format: TextureFormat,
    )

    private val attachments = mutableSetOf<Attachment>()

    private var size: Pair<Int, Int>? = null

    override fun setSize(width: Int, height: Int) {
        size = width to height
    }

    override fun setAttachments(attachments: Set<Attachment>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
    }

    fun build(): RenderTarget {
        val (width, height) = checkNotNull(size)
        val fbo = createFbo(width, height)
        val skijaContext = SkijaContext.create(width, height, fbo.handle)
        return RenderTargetImpl(fbo, skijaContext)
    }

    private fun createFbo(width: Int, height: Int): Framebuffer {
        check(attachments.contains(Attachment.COLOR))
        val fbo = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)

        // color attachment
        val colorAttachment =
            createTextureAttachment(GL_RGBA16, GL_RGBA, GL_COLOR_ATTACHMENT0, GL_FLOAT, width, height)

        // depth attachment
        var depthAttachment: Int? = null
        if (attachments.contains(Attachment.DEPTH)) {
            depthAttachment = createTextureAttachment(
                GL_DEPTH_COMPONENT,
                GL_DEPTH_COMPONENT,
                GL_DEPTH_ATTACHMENT,
                GL_FLOAT,
                width,
                height
            )
        }

        check(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE) {
            "Failed to initialize framebuffer. ${glCheckFramebufferStatus(GL_FRAMEBUFFER)}"
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        return Framebuffer(fbo, colorAttachment, depthAttachment)
    }

    private fun createTextureAttachment(
        internalFormat: Int,
        format: Int,
        attachmentType: Int,
        dataType: Int,
        width: Int,
        height: Int,
    ): Int {
        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            internalFormat,
            width,
            height,
            0,
            format,
            dataType,
            NULL
        )
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentType, GL_TEXTURE_2D, texture, 0)
        glBindTexture(GL_TEXTURE_2D, 0)
        return texture
    }
}