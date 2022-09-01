package calamansi.runtime.resource

import calamansi.Scene
import calamansi.runtime.helpers.EngineTest
import calamansi.runtime.helpers.TestComponent
import calamansi.runtime.helpers.TestScript
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ResourceModuleTest : EngineTest() {
    @Test
    fun scene() = runBlocking {
        val ref = resourceModule.fetchResource("test://sample.scn")
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