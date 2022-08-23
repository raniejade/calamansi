package calamansi.math

import kotlin.math.sqrt

// TODO: convert to value class once https://youtrack.jetbrains.com/issue/KT-24874
//  is implemented
//@JvmInline
/*value*/ class Quaternion private constructor(@PublishedApi internal val buffer: FloatArray) {
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

    inline var w: Float
        get() = buffer[3]
        set(value) {
            buffer[3] = value
        }

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 1f) : this(floatArrayOf(x, y, z, w))

    operator fun times(scalar: Float): Quaternion {
        return Quaternion(x * scalar, y * scalar, z * scalar, w * scalar)
    }

    operator fun timesAssign(scalar: Float) {
        x *= scalar
        y *= scalar
        z *= scalar
        w *= scalar
    }

    operator fun times(other: Quaternion): Quaternion {
        return Quaternion(
            (w * other.x) + (x * other.w) + (y * other.z) - (z * other.y),
            (w * other.y) - (x * other.z) + (y * other.w) + (z * other.x),
            (w * other.z) + (x * other.y) - (y * other.x) + (z * other.w),
            (w * other.w) - (x * other.x) - (y * other.y) - (z * other.z)
        )
    }

    operator fun timesAssign(other: Quaternion) {
        x = (w * other.x) + (x * other.w) + (y * other.z) - (z * other.y)
        y = (w * other.y) - (x * other.z) + (y * other.w) + (z * other.x)
        z = (w * other.z) + (x * other.y) - (y * other.x) + (z * other.w)
        w = (w * other.w) - (x * other.x) - (y * other.y) - (z * other.z)
    }

    operator fun unaryPlus(): Quaternion {
        return Quaternion(+x, +y, +z, +w)
    }

    operator fun unaryMinus(): Quaternion {
        return Quaternion(-x, -y, -z, -w)
    }

    fun normalize(): Quaternion {
        val length = length()
        check(length != 0f) { "length must not be 0" }
        x /= length
        y /= length
        z /= length
        w /= length
        return this
    }

    fun normalized(): Quaternion {
        val length = length()
        check(length != 0f) { "length must not be 0" }
        return Quaternion(x / length, y / length, z / length, w / length)
    }

    fun isNormalized(): Boolean = length() == 1f

    fun length(): Float {
        return sqrt((x * x) + (y * y) + (z * z) + (w * w))
    }

    fun lengthSquared(): Float {
        val length = length()
        return length * length
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Quaternion) {
            return false
        }
        return buffer.contentEquals(other.buffer)
    }

    override fun hashCode(): Int {
        return buffer.hashCode()
    }

    override fun toString(): String {
        return "Quaternion(x=$x, y=$y, z=$z, w=$w)"
    }
}