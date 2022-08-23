package calamansi.math

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Vector3fTest {
    @Test
    fun plus() {
        val a = Vector3f(1f, 2f, 3f)
        val b = Vector3f(0f, 3f, 2f)
        val result = a + b
        assertEquals(a.x + b.x, result.x)
        assertEquals(a.y + b.y, result.y)
        assertEquals(a.z + b.z, result.z)
    }

    @Test
    fun plusAssign() {
        val a = Vector3f(1f, 2f, 1f)
        a+= Vector3f(1f, -1f, 2f)
        assertEquals(a.x, 2f)
        assertEquals(a.y, 1f)
        assertEquals(a.z, 3f)
    }

    @Test
    fun minus() {
        val a = Vector3f(1f, 2f, 0f)
        val b = Vector3f(0f, 3f, 2f)
        val result = a - b
        assertEquals(a.x - b.x, result.x)
        assertEquals(a.y - b.y, result.y)
        assertEquals(a.z - b.z, result.z)
    }

    @Test
    fun minusAssign() {
        val a = Vector3f(1f, 2f, 3f)
        a-= Vector3f(1f, 1f, 1f)
        assertEquals(a.x, 0f)
        assertEquals(a.y, 1f)
        assertEquals(a.z, 2f)
    }

    @Test
    fun times() {
        val a = Vector3f(1f, 2f, -1f)
        val scalar = -3f
        val result = a * scalar
        assertEquals(a.x * scalar, result.x)
        assertEquals(a.y * scalar, result.y)
        assertEquals(a.z * scalar, result.z)
    }

    @Test
    fun div() {
        val a = Vector3f(1f, 2f, 1f)
        val scalar = -3f
        val result = a / scalar
        assertEquals(a.x / scalar, result.x)
        assertEquals(a.y / scalar, result.y)
        assertEquals(a.z / scalar, result.z)
    }

    @Test
    fun length() {
        val a = Vector3f(2f, 3f, -1f)
        val expected = sqrt((a.x * a.x) + (a.y * a.y) + (a.z * a.z))
        assertEquals(expected, a.length())
    }

    @Test
    fun lengthSquared() {
        val a = Vector3f(2f, 3f, -5f)
        assertEquals(a.length() * a.length(), a.lengthSquared())
    }

    @Test
    fun normalize() {
        val a = Vector3f(2f, 3f, -1f)
        val x = a.x
        val y = a.y
        val z = a.z
        val length = a.length()
        a.normalize()
        assertEquals(Vector3f(x / length, y / length, z / length), a)
        assertEquals(1f, a.length(), EPSILON)
        assertTrue(a.isNormalized())
    }

    @Test
    fun normalized() {
        val a = Vector3f(2f, 3f, -1f)
        val length = a.length()
        val unit = a.normalized()
        // a must not be touched
        assertEquals(Vector3f(2f, 3f, -1f), a)
        assertEquals(Vector3f(a.x / length, a.y / length, a.z / length), unit)
        assertEquals(1f, unit.length(), EPSILON)
        assertTrue(unit.isNormalized())
    }

    @Test
    fun dot() {
        val a = Vector3f(10f , 5f, 2f)
        val b = Vector3f(8f, 11f, -1f)
        assertEquals((a.x * b.x) + (a.y * b.y) + (a.z * b.z), a dot b)
    }
}