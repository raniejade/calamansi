package calamansi.runtime.resource

import calamansi.runtime.helpers.EngineTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.joml.*
import kotlin.test.Test
import kotlin.test.assertEquals

class JomlSerializationTest : EngineTest() {
    private val json = resourceModule.getJsonSerializer()

    @Test
    fun vector2f() {
        val value = Vector2f(2f, 1f)
        val rawJson = json.encodeToString(value)
        val decoded = json.decodeFromString<Vector2f>(rawJson)
        assertEquals(value, decoded)
    }

    @Test
    fun vector3f() {
        val value = Vector3f(2f, 1f, -10f)
        val rawJson = json.encodeToString(value)
        val decoded = json.decodeFromString<Vector3f>(rawJson)
        assertEquals(value, decoded)
    }

    @Test
    fun matrix4f() {
        val value = Matrix4f().translate(10f, 25f, -10.5f)
            .rotate(Math.toRadians(30f), Vector3f(0f, 1f, 0f))
            .scale(0.2f)
        val rawJson = json.encodeToString(value)
        val decoded = json.decodeFromString<Matrix4f>(rawJson)
        assertEquals(value, decoded)
    }

    @Test
    fun matrix3x2f() {
        val value = Matrix3x2f().translate(10f, -25f)
            .rotate(Math.toRadians(30f))
            .scale(0.2f)
        val rawJson = json.encodeToString(value)
        val decoded = json.decodeFromString<Matrix3x2f>(rawJson)
        assertEquals(value, decoded)
    }
}