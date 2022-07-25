package calamansi.math

@JvmInline
value class Vector3f(private val vec: org.joml.Vector3f) {
    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f): this(org.joml.Vector3f(x, y, z))

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

    var z: Float
        get() = vec.z
        set(value) {
            vec.z = value
        }


    fun length() = vec.length()
    fun lengthSquared() = vec.lengthSquared()

    operator fun plus(other: Vector3f): Vector3f {
        return Vector3f(org.joml.Vector3f(vec).add(other.vec))
    }

    operator fun plusAssign(other: Vector3f) {
        vec.add(other.vec)
    }

    operator fun minus(other: Vector3f): Vector3f {
        return Vector3f(org.joml.Vector3f(vec).sub(other.vec))
    }

    operator fun minusAssign(other: Vector3f) {
        vec.sub(other.vec)
    }

    operator fun times(scalar: Float): Vector3f {
        return Vector3f(org.joml.Vector3f(vec).mul(scalar))
    }

    operator fun timesAssign(scalar: Float) {
        vec.mul(scalar)
    }

    operator fun div(scalar: Float): Vector3f {
        return Vector3f(org.joml.Vector3f(vec).div(scalar))
    }

    operator fun divAssign(scalar: Float) {
        vec.div(scalar)
    }

    override fun toString(): String {
        return vec.toString()
    }
}