package calamansi.runtime.serializer

import calamansi.runtime.data.SerializedNode
import calamansi.runtime.data.SerializedScene
import calamansi.runtime.helpers.TestComponent
import calamansi.runtime.helpers.autoRegisterTestDefinitions
import calamansi.runtime.logging.ConsoleLogger
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.registry.RuntimeRegistry
import java.io.ByteArrayOutputStream
import kotlin.test.*

class SceneSerializerTest {
    private val registry = RuntimeRegistry(ConsoleLogger(LogLevel.INFO))
    private lateinit var serializer: Serializer

    @BeforeTest
    fun setup() {
        registry.autoRegisterTestDefinitions()
        serializer = Serializer(registry.serializersModule)
    }

    @Test
    fun decodeScene() {
        // language=JSON
        val raw = """
            {
                "nodes": [
                    {
                        "name": "root",
                        "script": "calamansi.runtime.helpers.TestScript",
                        "components": [
                            {
                                "type": "calamansi.runtime.helpers.TestComponent.Data",
                                "int": 25
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()


        val scene = serializer.decodeScene(raw.byteInputStream())
        assertEquals(1, scene.nodes.size)
        val serializedNode = scene.nodes[0]
        assertEquals("root", serializedNode.name)
        assertEquals("calamansi.runtime.helpers.TestScript", serializedNode.script)
        assertEquals(1, serializedNode.components.size)
        val data = assertIs<TestComponent.Data>(serializedNode.components[0])
        assertEquals(25, data.int)
    }

    @Test
    fun decodeEmptyScene() {
        // language=JSON
        val raw = """
            {}
        """.trimIndent()

        val scene = serializer.decodeScene(raw.byteInputStream())
        assertTrue(scene.nodes.isEmpty())
    }

    @Test
    fun encodeScene() {
        val os = ByteArrayOutputStream()
        val scene = SerializedScene(
            listOf(
                SerializedNode("root", null, "some.Script", listOf(TestComponent.Data(55))),
                SerializedNode("root", 0, null, listOf(TestComponent.Data(23))),
            )
        )

        serializer.encodeScene(scene, os)
        val decodedScene = serializer.decodeScene(os.toString().byteInputStream())
        assertEquals(scene, decodedScene)
    }
}