package calamansi.runtime

import calamansi.runtime.helpers.TestComponent
import calamansi.runtime.helpers.autoRegisterTestDefinitions
import calamansi.runtime.logging.ConsoleLogger
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.registry.RuntimeRegistry
import kotlin.test.*

class NodeTest {
    private val logger = ConsoleLogger(LogLevel.INFO)
    private val registry = RuntimeRegistry(logger)
    private val componentManager = ComponentManager(registry)
    private val scriptManager = ScriptManager(registry)
    private val executionContext = ExecutionContextImpl(logger, componentManager, scriptManager)

    @BeforeTest
    fun setup() {
        registry.autoRegisterTestDefinitions()
    }

    @Test
    fun cycles() {
        val root = NodeImpl("root", componentManager, null)
        val child  = NodeImpl("child", componentManager, null)

        with(executionContext) {
            root.addChild(child)
            // should not add root to child, i.e. return false
            assertFalse(child.addChild(root))
        }
    }

    @Test
    fun addComponent() {
        val node = NodeImpl("root", componentManager, null)
        with(executionContext) {
            assertFalse(node.hasComponent(TestComponent::class))
            node.addComponent(TestComponent::class)
            assertTrue(node.hasComponent(TestComponent::class))
        }
    }

    @Test
    fun addSameComponent() {
        val node = NodeImpl("root", componentManager, null)
        with(executionContext) {
            node.addComponent(TestComponent::class)
            assertFails { node.addComponent(TestComponent::class) }
        }
    }

    @Test
    fun addComponentAgainAfterRemoval() {
        val node = NodeImpl("root", componentManager, null)
        with(executionContext) {
            val instance1 = node.addComponent(TestComponent::class)
            node.removeComponent(TestComponent::class)
            val instance2 = node.addComponent(TestComponent::class)
            assertNotSame(instance2, instance1)
        }
    }

    @Test
    fun removeComponent() {
        val node = NodeImpl("root", componentManager, null)
        with(executionContext) {
            assertFalse(node.removeComponent(TestComponent::class))
            node.addComponent(TestComponent::class)
            assertTrue(node.removeComponent(TestComponent::class))
            assertFalse(node.hasComponent(TestComponent::class))
        }
    }
}