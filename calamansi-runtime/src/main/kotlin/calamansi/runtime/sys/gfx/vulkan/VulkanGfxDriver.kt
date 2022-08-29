package calamansi.runtime.sys.gfx.vulkan

import calamansi.runtime.sys.gfx.Gfx
import calamansi.runtime.sys.gfx.GfxDriver


object VulkanGfxDriver : GfxDriver {
    override fun init() {
    }

    override fun create(): Gfx {
        return VulkanGfx()
    }

    override fun shutdown() {
    }
}