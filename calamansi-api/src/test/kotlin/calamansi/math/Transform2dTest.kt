package calamansi.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Transform2dTest {
    @Test
    fun translation() {
        val m = Transform2d().translate(10f, -5f)
        assertEquals(Vector2f(5f, 4f), m.transform(Vector2f(-5f, 9f)))
    }

    @Test
    fun scale() {
        val m = Transform2d().scale(2f, -0.5f)
        assertEquals(Vector2f(-4f, -25f), m.transform(Vector2f(-2f, 50f)))
    }

    @Test
    fun composeFluent() {
        val m1 = Transform2d().translate(10f, 2f)
            .scale(2f, 1f)

        val m2 = Transform2d().scale(2f , 1f)
            .translate(10f, 2f)

        val point = Vector2f()

        assertEquals(Vector2f(10f, 2f), m1.transform(point))
        assertEquals(Vector2f(20f, 2f), m2.transform(point))
        assertNotEquals(m1.transform(point), m2.transform(point))
    }

    @Test
    fun composeUsingTimes() {
        val m1 = Transform2d().translate(10f, 2f)
        val m2 = Transform2d().scale(2f , 1f)

        assertEquals(m1 * m2, Transform2d().translate(10f, 2f).scale(2f, 1f))
        assertEquals(m2 * m1, Transform2d().scale(2f, 1f).translate(10f, 2f))
    }
}