package calamansi.math

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

// TODO: convert to value class once https://youtrack.jetbrains.com/issue/KT-24874
//  is implemented
//@JvmInline
@Serializable
/*value*/ class Vector2f internal constructor(@PublishedApi internal val buffer: FloatArray) {
    constructor(x: Float = 0f, y: Float = 0f) : this(floatArrayOf(x, y))

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

    operator fun plus(other: Vector2f): Vector2f {
        return Vector2f(x + other.x, y + other.y)
    }

    operator fun plusAssign(other: Vector2f) {
        x += other.x
        y += other.y
    }

    operator fun minus(other: Vector2f): Vector2f {
        return Vector2f(x - other.x, y - other.y)
    }

    operator fun minusAssign(other: Vector2f) {
        x -= other.x
        y -= other.y
    }

    operator fun times(scalar: Float): Vector2f {
        return Vector2f(x * scalar, y * scalar)
    }

    operator fun div(scalar: Float): Vector2f {
        require(scalar != 0f) { "scalar must not be 0" }
        return Vector2f(x / scalar, y / scalar)
    }

    operator fun unaryMinus(): Vector2f {
        return Vector2f(-x, -y)
    }

    operator fun unaryPlus(): Vector2f {
        return Vector2f(+x, +y)
    }

    fun unit(): Vector2f {
        val length = length()
        check(length != 0f) { "length must not be 0" }
        return Vector2f(x / length, y / length)
    }

    infix fun dot(other: Vector2f): Float {
        return (x * other.x) + (y * other.y)
    }

    fun length(): Float {
        return sqrt((x * x) + (y * y))
    }

    fun lengthSquared(): Float {
        val length = length()
        return length * length
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Vector2f) {
            return false
        }
        return buffer.contentEquals(other.buffer)
    }

    override fun hashCode(): Int {
        return buffer.hashCode()
    }

    override fun toString(): String {
        return "Vector2f(x=$x, y=$y)"
    }
}