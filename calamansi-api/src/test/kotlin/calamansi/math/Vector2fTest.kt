package calamansi.math

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class Vector2fTest {
    @Test
    fun plus() {
        val a = Vector2f(1f, 2f)
        val b = Vector2f(0f, 3f)
        val result = a + b
        assertEquals(a.x + b.x, result.x)
        assertEquals(a.y + b.y, result.y)
    }

    @Test
    fun plusAssign() {
        val a = Vector2f(1f, 2f)
        a+= Vector2f(1f, -1f)
        assertEquals(a.x, 2f)
        assertEquals(a.y, 1f)
    }

    @Test
    fun minus() {
        val a = Vector2f(1f, 2f)
        val b = Vector2f(0f, 3f)
        val result = a - b
        assertEquals(a.x - b.x, result.x)
        assertEquals(a.y - b.y, result.y)
    }

    @Test
    fun minusAssign() {
        val a = Vector2f(1f, 2f)
        a-= Vector2f(1f, 1f)
        assertEquals(a.x, 0f)
        assertEquals(a.y, 1f)
    }

    @Test
    fun times() {
        val a = Vector2f(1f, 2f)
        val scalar = -3f
        val result = a * scalar
        assertEquals(a.x * scalar, result.x)
        assertEquals(a.y * scalar, result.y)
    }

    @Test
    fun div() {
        val a = Vector2f(1f, 2f)
        val scalar = -3f
        val result = a / scalar
        assertEquals(a.x / scalar, result.x)
        assertEquals(a.y / scalar, result.y)
    }

    @Test
    fun length() {
        val a = Vector2f(2f, 3f)
        val expected = sqrt((a.x * a.x) + (a.y * a.y))
        assertEquals(expected, a.length())
    }

    @Test
    fun lengthSquared() {
        val a = Vector2f(2f, 3f)
        assertEquals(a.length() * a.length(), a.lengthSquared())
    }

    @Test
    fun unit() {
        val a = Vector2f(2f, 3f)
        val length = a.length()
        val unit = a.unit()
        assertEquals(Vector2f(a.x / length, a.y / length), unit)
        assertEquals(1f, unit.length(), 0.001f)
    }

    @Test
    fun dot() {
        val a = Vector2f(10f , 5f)
        val b = Vector2f(8f, 11f)
        assertEquals((a.x * b.x) + (a.y * b.y), a dot b)
    }
}