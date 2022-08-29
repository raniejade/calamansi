package calamansi.runtime.sys.gfx.vulkan

import calamansi.runtime.sys.gfx.Frame
import calamansi.runtime.sys.gfx.Gfx
import calamansi.runtime.sys.gfx.Surface
import calamansi.runtime.sys.window.Window
import calamansi.runtime.sys.window.glfw.GlfwWindow
import org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback

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

class VulkanFrame(val surface: VulkanSurface) : Frame {
    override fun submit() {
        // swap chain
    }
}

class VulkanGfx : Gfx {
    override fun createSurface(window: Window): Surface {
        return VulkanSurface(window)
    }

    override fun startFrame(surface: Surface): Frame {
        require(surface is VulkanSurface)
        return VulkanFrame(surface)
    }
}