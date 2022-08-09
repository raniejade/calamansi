package calamansi.math

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Vector2f(@Contextual private val vec: org.joml.Vector2f) {
    constructor(x: Float = 0f, y: Float = 0f) : this(org.joml.Vector2f(x, y))

    var x: Float
        get() = vec.x
        set(value) {
            vec.x = value
        }

    var y: Float
        get() = vec.y
        set(value) {
            vec.y = value
        }


    fun length() = vec.length()
    fun lengthSquared() = vec.lengthSquared()

    operator fun plus(other: Vector2f): Vector2f {
        return Vector2f(org.joml.Vector2f(vec).add(other.vec))
    }

    operator fun plusAssign(other: Vector2f) {
        vec.add(other.vec)
    }

    operator fun minus(other: Vector2f): Vector2f {
        return Vector2f(org.joml.Vector2f(vec).sub(other.vec))
    }

    operator fun minusAssign(other: Vector2f) {
        vec.sub(other.vec)
    }

    operator fun times(scalar: Float): Vector2f {
        return Vector2f(org.joml.Vector2f(vec).mul(scalar))
    }

    operator fun timesAssign(scalar: Float) {
        vec.mul(scalar)
    }

    operator fun div(scalar: Float): Vector2f {
        return Vector2f(org.joml.Vector2f(vec).div(scalar))
    }

    operator fun divAssign(scalar: Float) {
        vec.div(scalar)
    }

    override fun toString(): String {
        return vec.toString()
    }
}