package calamansi.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Transform3dTest {
    @Test
    fun identity() {
        assertTrue(Transform3d().isIdentity())
    }

    @Test
    fun translate() {
        val m = Transform3d().translate(10f, -5f, 25f)
        assertEquals(Vector3f(5f, 4f, 20f), m.transform(Vector3f(-5f, 9f, -5f)))
    }

    @Test
    fun translated() {
        val original = Transform3d()
        val m = original.translated(10f, -5f, 25f)
        assertTrue(original.isIdentity())
        assertEquals(Vector3f(5f, 4f, 20f), m.transform(Vector3f(-5f, 9f, -5f)))
    }

    @Test
    fun scale() {
        val m = Transform3d().scale(2f, -0.5f, 0.2f)
        assertEquals(Vector3f(-4f, -25f, 2f), m.transform(Vector3f(-2f, 50f, 10f)))
    }

    @Test
    fun scaled() {
        val original = Transform3d()
        val m = original.scaled(2f, -0.5f, 0.2f)
        assertTrue(original.isIdentity())
        assertEquals(Vector3f(-4f, -25f, 2f), m.transform(Vector3f(-2f, 50f, 10f)))
    }

    @Test
    fun composeFluent() {
        val m1 = Transform3d().translate(10f, 2f, -20f)
            .scale(2f, 1f, 0.5f)

        val m2 = Transform3d().scale(2f , 1f, 0.5f)
            .translate(10f, 2f, -20f)

        val point = Vector3f()
        assertEquals(Vector3f(10f, 2f, -20f), m1.transform(point))
        assertEquals(Vector3f(20f, 2f, -10f), m2.transform(point))
        assertNotEquals(m1.transform(point), m2.transform(point))
    }

    @Test
    fun composeUsingTimes() {
        val m1 = Transform3d().translate(10f, 2f, -0.5f)
        val m2 = Transform3d().scale(2f , 1f, 2f)

        assertEquals(m1 * m2, Transform3d().translate(10f, 2f, -0.5f).scale(2f, 1f, 2f))
        assertEquals(m2 * m1, Transform3d().scale(2f, 1f, 2f).translate(10f, 2f, -0.5f))
    }
}