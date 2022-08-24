package calamansi.runtime

import calamansi.ExecutionContext
import calamansi.Script
import calamansi.event.Event
import calamansi.runtime.helpers.ComponentWithDependency
import calamansi.runtime.helpers.ComponentWithNestedDependency
import calamansi.runtime.helpers.EngineTest
import calamansi.runtime.helpers.TestComponent
import calamansi.window.WindowCloseEvent
import java.lang.RuntimeException
import kotlin.test.*
class EventCountingScript : Script() {
    context(ExecutionContext) override fun handleEvent(event: Event) {
        counter++
    }

    companion object {
        var counter = 0
    }
}

class EventConsumingScript : Script() {
    context(ExecutionContext) override fun handleEvent(event: Event) {
        event.consume()
    }
}

class EventFailingScript : Script() {
    context(ExecutionContext) override fun handleEvent(event: Event) {
        throw RuntimeException()
    }
}

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
    fun addComponentWithDependency() {
        val node = NodeImpl("root", null)
        // fail, due to missing required component
        assertFails { node.addComponent(ComponentWithDependency::class) }
        // add required component
        node.addComponent(TestComponent::class)
        // add should succeed
        node.addComponent(ComponentWithDependency::class)
    }

    @Test
    fun addDeeplyNestedComponentDeps() {
        val node = NodeImpl("root", null)
        // fail, due to missing required component
        assertFails { node.addComponent(ComponentWithNestedDependency::class) }
        assertFails { node.addComponent(ComponentWithDependency::class) }
        // add required components
        node.addComponent(TestComponent::class)
        node.addComponent(ComponentWithDependency::class)
        node.addComponent(ComponentWithNestedDependency::class)
    }

    @Test
    fun removeComponentWithDependency() {
        val node = NodeImpl("root", null)
        node.addComponent(TestComponent::class)
        node.addComponent(ComponentWithDependency::class)
        // fails because ComponentWithDependency requires TestComponent
        assertFalse(node.removeComponent<TestComponent>())
        // now remove dependent component
        assertTrue(node.removeComponent<ComponentWithDependency>())
        // we should be able to remove it
        assertTrue(node.removeComponent<TestComponent>())
    }

    @Test
    fun removeDeeplyNestedComponentDeps() {
        val node = NodeImpl("root", null)
        node.addComponent(TestComponent::class)
        node.addComponent(ComponentWithDependency::class)
        node.addComponent(ComponentWithNestedDependency::class)

        // fails due to dependency
        assertFalse(node.removeComponent<TestComponent>())
        assertFalse(node.removeComponent<ComponentWithDependency>())

        // remove dependent component
        assertTrue(node.removeComponent<ComponentWithNestedDependency>())
        // we still can't remove TestComponent
        assertFalse(node.removeComponent<TestComponent>())
        // but we can remove ComponentWithDependency
        assertTrue(node.removeComponent<ComponentWithDependency>())
        // we should be able to remove TestComponent now
        assertTrue(node.removeComponent<TestComponent>())
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

    @Test
    fun consumedEventNotPropagated() {

        val root = NodeImpl("root", EventConsumingScript::class.qualifiedName)
        root.isSceneRoot = true
        val child = NodeImpl("root", EventFailingScript::class.qualifiedName)
        root.addChild(child)

        // this should not fail
        root.handleEvent(WindowCloseEvent())
    }

    @Test
    fun eventPropagated() {

        val root = NodeImpl("root", EventCountingScript::class.qualifiedName)
        root.isSceneRoot = true
        root.addChild(NodeImpl("child", EventCountingScript::class.qualifiedName))
        root.addChild(NodeImpl("some-other-child", EventCountingScript::class.qualifiedName))

        root.handleEvent(WindowCloseEvent())
        assertEquals(3, EventCountingScript.counter)
    }
}