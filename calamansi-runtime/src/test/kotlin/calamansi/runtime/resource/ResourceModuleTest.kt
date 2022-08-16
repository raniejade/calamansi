package calamansi.runtime.resource

import calamansi.Scene
import calamansi.runtime.helpers.EngineTest
import calamansi.runtime.helpers.TestComponent
import calamansi.runtime.helpers.TestScript
import kotlin.test.*

class ResourceModuleTest : EngineTest() {
    @Test
    fun scene() {
        val ref = resourceModule.loadResource("test://sample.scn")
        val scene = ref.get() as Scene
        val root  = assertNotNull(scene.create())
        assertEquals("root", root.name)
        assertFalse(root.hasChildren())
        assertIs<TestScript>(root.script)
        assertTrue(root.hasComponent<TestComponent>())
        val component = root.getComponent<TestComponent>()
        assertEquals(1024, component.int)
    }
}