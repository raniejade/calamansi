package calamansi.runtime

import kotlin.test.Test
import kotlin.test.assertFails

class NodeTest {
    @Test
    fun cycles() {
        val root = NodeImpl("root", null)
        val child = NodeImpl("child", null)

        root.addChild(child)
        assertFails { child.addChild(root) }
    }
}