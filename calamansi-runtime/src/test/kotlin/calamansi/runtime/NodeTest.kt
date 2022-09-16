package calamansi.runtime

import calamansi.node.Node
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFails

class NodeTest {
    @Test
    @Ignore
    fun cycles() {
        val root = Node()
        val child = Node()

        root.addChild(child)
        assertFails { child.addChild(root) }
    }
}