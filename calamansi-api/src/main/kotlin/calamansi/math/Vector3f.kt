package calamansi.math

import kotlin.math.sqrt

@JvmInline
value class Vector3f internal constructor(@PublishedApi internal val buffer: FloatArray) {
    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f) : this(floatArrayOf(x, y, z))

    inline var x: Float
        get() = buffer[0]
        set(value) {
            buffer[0] = value
        }

    inline var y: Float
        get() = buffer[1]
        set(value) {
            buffer[1] = value
        }

    inline var z: Float
        get() = buffer[2]
        set(value) {
            buffer[2] = value
        }

    operator fun plus(other: Vector3f): Vector3f {
        return Vector3f(x + other.x, y + other.y, z + other.z)
    }

    operator fun plusAssign(other: Vector3f) {
        x += other.x
        y += other.y
        z += other.z
    }

    operator fun minus(other: Vector3f): Vector3f {
        return Vector3f(x - other.x, y - other.y, z - other.z)
    }

    operator fun minusAssign(other: Vector3f) {
        x -= other.x
        y -= other.y
        z -= other.z
    }

    operator fun times(scalar: Float): Vector3f {
        return Vector3f(x * scalar, y * scalar, z * scalar)
    }

    operator fun div(scalar: Float): Vector3f {
        require(scalar != 0f) { "scalar must not be 0" }
        return Vector3f(x / scalar, y / scalar, z / scalar)
    }

    operator fun unaryMinus(): Vector3f {
        return Vector3f(-x, -y, -z)
    }

    operator fun unaryPlus(): Vector3f {
        return Vector3f(+x, +y, +z)
    }

    fun unit(): Vector3f {
        val length = length()
        check(length != 0f) { "length must not be 0" }
        return Vector3f(x / length, y / length, z / length)
    }

    infix fun dot(other: Vector3f): Float {
        return (x * other.x) + (y * other.y) + (z * other.z)
    }

    infix fun cross(other: Vector3f): Vector3f {
        TODO()
    }

    fun length(): Float {
        return sqrt((x * x) + (y * y) + (z * z))
    }

    fun lengthSquared(): Float {
        val length = length()
        return length * length
    }

    override fun toString(): String {
        return "Vector2f(x=$x, y=$y, z=$z)"
    }
}