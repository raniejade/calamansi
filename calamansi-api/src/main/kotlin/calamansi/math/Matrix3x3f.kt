package calamansi.math

// TODO: convert to value class once https://youtrack.jetbrains.com/issue/KT-24874
//  is implemented
//@JvmInline
/*value*/ class Matrix3x3f private constructor(private val buffer: FloatArray) {
    constructor() : this(
        floatArrayOf(
            1f, 0f, 0f, /* basis x */
            0f, 1f, 0f, /* basis y */
            0f, 0f, 1f, /* translation */
        )
    )

    fun translate(x: Float = 0f, y: Float = 0f): Matrix3x3f {
        val translation = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            x, y, 1f,
        )
        compose(buffer, translation, buffer)
        return this
    }

    fun scale(x: Float = 1f, y: Float = 1f): Matrix3x3f {
        val scale = floatArrayOf(
            x, 0f, 0f,
            0f, y, 0f,
            0f, 0f, 1f,
        )
        compose(buffer, scale, buffer)
        return this
    }

    inline fun transform(vec: Vector2f): Vector2f {
        return times(vec)
    }

    operator fun times(other: Matrix3x3f): Matrix3x3f {
        val result = FloatArray(3 * 3)
        compose(buffer, other.buffer, result)
        return Matrix3x3f(result)
    }

    operator fun times(vec: Vector2f): Vector2f {
        val x = vec.x
        val y = vec.y
        val z = 1f

        val ix = buffer[0]
        val iy = buffer[1]
        //val iz = buffer[2]
        val jx = buffer[3]
        val jy = buffer[4]
        //val jz = buffer[5]
        val kx = buffer[6]
        val ky = buffer[7]
        //val kz = buffer[8]

        // (x * [ix, iy, iz]) + (y * [jx, jy, jz]) + (z * [kx, ky, kz])
        val tx = (x * ix) + (y * jx) + (z * kx)
        val ty = (x * iy) + (y * jy) + (z * ky)
        //val tz = (x * iz) + (y * jz) + (z * kz)


        return Vector2f(tx, ty)
    }

    operator fun get(idx: Int): Float = buffer[idx]

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Matrix3x3f) {
            return false
        }
        return buffer.contentEquals(other.buffer)
    }

    override fun hashCode(): Int {
        return buffer.hashCode()
    }

    override fun toString(): String {
        return """
            [${get(0)}, ${get(3)}, ${get(6)}]
            [${get(1)}, ${get(4)}, ${get(7)}]
            [${get(2)}, ${get(5)}, ${get(8)}]
        """.trimIndent()
    }

    companion object {
        // dest = a * b
        private inline fun compose(a: FloatArray, b: FloatArray, dest: FloatArray) {
            val ix = b[0]
            val iy = b[1]
            val iz = b[2]
            val jx = b[3]
            val jy = b[4]
            val jz = b[5]
            val kx = b[6]
            val ky = b[7]
            val kz = b[8]

            val xx = a[0]
            val xy = a[1]
            val xz = a[2]
            val yx = a[3]
            val yy = a[4]
            val yz = a[5]
            val zx = a[6]
            val zy = a[7]
            val zz = a[8]

            // (ix * [xx, xy, xz]) + (iy * [yx, yy, yz]) + (iz * [zx, zy, zz])
            val txx = (ix * xx) + (iy * yx) + (iz * zx)
            val txy = (ix * xy) + (iy * yy) + (iz * zy)
            val txz = (ix * xz) + (iy * yz) + (iz * zz)

            // (jx * [xx, xy, xz]) + (jy * [yx, yy, yz]) + (jz * [zx, zy, zz])
            val tyx = (jx * xx) + (jy * yx) + (jz * zx)
            val tyy = (jx * xy) + (jy * yy) + (jz * zy)
            val tyz = (jx * xz) + (jy * yz) + (jz * zz)

            // (kx * [xx, xy, xz]) + (ky * [yx, yy, yz]) + (kz * [zx, zy, zz])
            val tzx = (kx * xx) + (ky * yx) + (kz * zx)
            val tzy = (kx * xy) + (ky * yy) + (kz * zy)
            val tzz = (kx * xz) + (ky * yz) + (kz * zz)

            dest[0] = txx
            dest[1] = txy
            dest[2] = txz
            dest[3] = tyx
            dest[4] = tyy
            dest[5] = tyz
            dest[6] = tzx
            dest[7] = tzy
            dest[8] = tzz
        }
    }
}