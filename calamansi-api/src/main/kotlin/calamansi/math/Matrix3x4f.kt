package calamansi.math

@JvmInline
value class Matrix3x4f private constructor(private val buffer: FloatArray) {
    constructor() : this(
        floatArrayOf(
            1f, 0f, 0f, /* basis x */
            0f, 1f, 0f, /* basis y */
            0f, 0f, 1f, /* basis z */
            0f, 0f, 0f, /* translation */
        )
    )

    operator fun times(other: Matrix3x4f): Matrix3x4f {
        TODO()
    }

    operator fun times(vec: Vector3f): Vector3f {
        TODO()
    }
}