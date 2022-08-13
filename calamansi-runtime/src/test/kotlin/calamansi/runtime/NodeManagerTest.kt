package calamansi.runtime

import calamansi.runtime.data.SerializedNode
import calamansi.runtime.data.SerializedScene
import calamansi.runtime.helpers.TestComponent
import calamansi.runtime.helpers.TestScript
import calamansi.runtime.helpers.autoRegisterTestDefinitions
import calamansi.runtime.logging.ConsoleLogger
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.registry.RuntimeRegistry
import kotlin.test.*

class NodeManagerTest {
    private val logger = ConsoleLogger(LogLevel.INFO)
    private var registry = RuntimeRegistry(logger)
    private var scriptManager = ScriptManager(registry)
    private var componentManager = ComponentManager(registry)
    private var nodeManager = NodeManager(componentManager, scriptManager)
    private var executionContext = ExecutionContextImpl(logger, componentManager, scriptManager)

    @BeforeTest
    fun setup() {
        registry.autoRegisterTestDefinitions()
    }

    @Test
    fun buildSceneGraph() {
        val serializedNodes = listOf(
            SerializedNode("root", null, checkNotNull(TestScript::class.qualifiedName), listOf(TestComponent.Data(10))),
            SerializedNode("root_child1", 0, null, emptyList()),
            SerializedNode("root_child1_child1", 1, null, listOf(TestComponent.Data(30))),
            SerializedNode("root_child2", 0, checkNotNull(TestScript::class.qualifiedName), emptyList()),
        )
        val serializedScene = SerializedScene(serializedNodes)

        with(executionContext) {
            val root = nodeManager.buildSceneGraph(serializedScene)
            assertNotNull(root)
            assertEquals("root", root.name)
            assertTrue(root.hasScript())
            assertTrue(root.script is TestScript)
            assertTrue(root.hasComponent(TestComponent::class))
            assertEquals(10, root.getComponent(TestComponent::class).int)
            val rootChildren = root.getChildren()
            assertEquals(2, rootChildren.size)

            val child1 = rootChildren[0]
            assertEquals("root_child1", child1.name)
            assertFalse(child1.hasComponent(TestComponent::class))
            assertFalse(child1.hasScript())
            val child1Children = child1.getChildren()
            assertEquals(1, child1Children.size)

            val child1Child1 = child1Children[0]
            assertEquals("root_child1_child1", child1Child1.name)
            assertTrue(child1Child1.hasComponent(TestComponent::class))
            assertEquals(30, child1Child1.getComponent(TestComponent::class).int)
            assertFalse(child1Child1.hasScript())

            val child2 = rootChildren[1]
            assertEquals("root_child2", child2.name)
            assertFalse(child2.hasComponent(TestComponent::class))
            assertTrue(child2.hasScript())
            assertTrue(child2.script is TestScript)
        }
    }

    @Test
    fun multiplePossibleRoots() {
        val serializedNodes = listOf(
            SerializedNode("root", null, null, emptyList()),
            SerializedNode("root_child1", 0, null, emptyList()),
            SerializedNode("ignored_root", null, null, emptyList()),
        )
        val serializedScene = SerializedScene(serializedNodes)

        with(executionContext) {
            val root = nodeManager.buildSceneGraph(serializedScene)
            assertNotNull(root)
            assertEquals("root", root.name)
            val rootChildren = root.getChildren()
            assertEquals(1, rootChildren.size)

            val child1 = rootChildren[0]
            assertEquals("root_child1", child1.name)
            assertTrue(child1.getChildren().isEmpty())
        }
    }

    @Test
    fun cycles() {
        val serializedNodes = listOf(
            SerializedNode("root", 2, null, emptyList()),
            SerializedNode("child1", 0, null, emptyList()),
            SerializedNode("child1_child1", 1, null, emptyList()),
        )
        val serializedScene = SerializedScene(serializedNodes)

        with(executionContext) {
            assertNull(nodeManager.buildSceneGraph(serializedScene))
        }
    }
}