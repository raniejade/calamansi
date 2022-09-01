package calamansi.runtime.resource

import calamansi.runtime.helpers.EngineTest
import calamansi.runtime.helpers.TestComponent
import calamansi.runtime.helpers.TestScript
import calamansi.runtime.helpers.dataType
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class SceneLoaderTest : EngineTest() {
    private lateinit var sceneLoader: SceneLoader

    @BeforeTest
    fun setup() {
        sceneLoader = SceneLoader()
    }

    @Test
    fun simple() = runBlocking {
        // language=JSON
        val raw = """
            {
                "nodes": [
                    {
                        "name": "root",
                        "script": "calamansi.runtime.helpers.TestScript",
                        "components": [
                            {
                                "type": ${dataType<TestComponent>()},
                                "int": 25
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()


        val scene = raw.byteInputStream().use {
            sceneLoader.load(it)
        }
        val root = assertNotNull(scene.resource.create())
        assertTrue(root.getChildren().isEmpty())
        assertEquals("root", root.name)
        assertTrue(root.hasComponent<TestComponent>())
        val component = root.getComponent<TestComponent>()
        assertEquals(25, component.int)
    }

    @Test
    fun multiplePossibleRoots() = runBlocking {
        // language=JSON
        val raw = """
            {
                "nodes": [
                    {
                        "name": "root",
                        "script": "calamansi.runtime.helpers.TestScript",
                        "components": [
                            {
                                "type": ${dataType<TestComponent>()},
                                "int": 25
                            }
                        ]
                    },
                    {
                        "name": "other_root"
                    }
                ]
            }
        """.trimIndent()

        val scene = raw.byteInputStream().use {
            sceneLoader.load(it)
        }
        val instance = scene.resource.create()
        assertNotNull(instance)
        assertTrue(instance.getChildren().isEmpty())
        assertEquals("root", instance.name)
    }

    @Test
    fun nested() = runBlocking {
        // language=JSON
        val raw = """
            {
                "nodes": [
                    {
                        "name": "root"
                    },
                    {
                        "name": "root_child1",
                        "parent": 0
                    },
                    {
                        "name": "root_child1_child",
                        "parent": 1,
                        "components": [
                            {
                                "type": ${dataType<TestComponent>()},
                                "int": -60
                            }
                        ]
                    },
                    {
                        "name": "root_child2",
                        "script": "${TestScript::class.qualifiedName}",
                        "parent": 0
                    }
                ]
            }
        """.trimIndent()


        val scene = raw.byteInputStream().use {
            sceneLoader.load(it)
        }
        val root = assertNotNull(scene.resource.create())
        assertEquals("root", root.name)
        assertNull(root.script)
        assertEquals(2, root.getChildren().size)

        val rootChild1 = root.getChildren()[0]
        assertEquals("root_child1", rootChild1.name)
        assertNull(rootChild1.script)
        assertEquals(1, rootChild1.getChildren().size)

        val rootChild1Child = rootChild1.getChildren()[0]
        assertEquals("root_child1_child", rootChild1Child.name)
        assertNull(rootChild1Child.script)
        assertFalse(rootChild1Child.hasChildren())
        assertTrue(rootChild1Child.hasComponent<TestComponent>())
        val component = rootChild1Child.getComponent<TestComponent>()
        assertEquals(-60, component.int)

        val rootChild2 = root.getChildren()[1]
        assertEquals("root_child2", rootChild2.name)
        assertIs<TestScript>(rootChild2.script)
        assertFalse(rootChild2.hasChildren())
    }

    @Test
    fun noRoot() = runBlocking {
        // language=JSON
        val raw = """
            {
                "nodes": [
                    {
                        "name": "root",
                        "parent": 2
                    },
                    {
                        "name": "root_child1",
                        "parent": 0
                    },
                    {
                        "name": "root_child1_child",
                        "parent": 1
                    }
                ]
            }
        """.trimIndent()

        val scene = raw.byteInputStream().use {
            sceneLoader.load(it)
        }
        assertNull(scene.resource.create())
    }

    @Test
    fun decodeEmptyScene() = runBlocking {
        // language=JSON
        val raw = """
            {}
        """.trimIndent()

        val scene = raw.byteInputStream().use {
            sceneLoader.load(it)
        }
        assertNull(scene.resource.create())
    }
}