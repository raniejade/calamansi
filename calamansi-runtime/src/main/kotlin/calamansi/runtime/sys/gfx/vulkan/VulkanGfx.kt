package calamansi.runtime.sys.gfx.vulkan

import calamansi.runtime.sys.gfx.*
import calamansi.runtime.sys.window.Window
import calamansi.runtime.sys.window.glfw.GlfwWindow
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback
import java.nio.FloatBuffer
import java.nio.IntBuffer

class VulkanSurface(private val window: Window) : Surface {
    init {
        if (window is GlfwWindow) {
            glfwSetFramebufferSizeCallback(window.handle) { _, width, height ->
                println("resized: $width x $height")
                // set require resize
            }
        }
    }

    override fun destroy() {
        TODO("Not yet implemented")
    }
}

class VulkanDraw(val surface: VulkanSurface) : Draw {
    override fun setProjectionMatrix(projection: Matrix4f) {
        TODO("Not yet implemented")
    }

    override fun setViewMatrix(view: Matrix4f) {
        TODO("Not yet implemented")
    }

    override fun uploadVertexBufferData(vertices: FloatBuffer, attributes: List<VertexAttribute>) {
        TODO("Not yet implemented")
    }

    override fun uploadIndexBufferData(indices: IntBuffer) {
        TODO("Not yet implemented")
    }

    override fun draw(mode: DrawMode) {
        TODO("Not yet implemented")
    }

    override fun drawInstanced(mode: DrawMode, count: Int) {
        TODO("Not yet implemented")
    }
}

class VulkanGfx : Gfx {
    override fun createSurface(window: Window): Surface {
        return VulkanSurface(window)
    }

    override fun startDraw(surface: Surface): Draw {
        require(surface is VulkanSurface)
        return VulkanDraw(surface)
    }
}