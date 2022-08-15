package calamansi.runtime

import calamansi.runtime.helpers.EngineTest
import calamansi.runtime.helpers.TestComponent
import kotlin.test.*

class NodeTest : EngineTest() {
    @Test
    fun cycles() {
        val root = NodeImpl("root", null)
        val child = NodeImpl("child", null)

        root.addChild(child)
        // should not add root to child, i.e. return false
        assertFalse(child.addChild(root))
    }

    @Test
    fun addComponent() {
        val node = NodeImpl("root", null)
        assertFalse(node.hasComponent(TestComponent::class))
        node.addComponent(TestComponent::class)
        assertTrue(node.hasComponent(TestComponent::class))
    }

    @Test
    fun addSameComponent() {
        val node = NodeImpl("root", null)
        node.addComponent(TestComponent::class)
        assertFails { node.addComponent(TestComponent::class) }
    }

    @Test
    fun addComponentAgainAfterRemoval() {
        val node = NodeImpl("root", null)
        val instance1 = node.addComponent(TestComponent::class)
        node.removeComponent(TestComponent::class)
        val instance2 = node.addComponent(TestComponent::class)
        assertNotSame(instance2, instance1)
    }

    @Test
    fun removeComponent() {
        val node = NodeImpl("root", null)
        assertFalse(node.removeComponent(TestComponent::class))
        node.addComponent(TestComponent::class)
        assertTrue(node.removeComponent(TestComponent::class))
        assertFalse(node.hasComponent(TestComponent::class))
    }
}