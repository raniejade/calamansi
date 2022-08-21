package calamansi.math

@JvmInline
value class Matrix4x4f private constructor(private val buffer: FloatArray) {
    constructor() : this(
        floatArrayOf(
            1f, 0f, 0f, 0f, /* basis x */
            0f, 1f, 0f, 0f, /* basis y */
            0f, 0f, 1f, 0f, /* basis z */
            0f, 0f, 0f, 1f, /* translation */
        )
    )

    fun translate(x: Float = 0f, y: Float = 0f, z: Float = 0f): Matrix4x4f {
        val translation = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            x, y, z, 1f,
        )
        compose(buffer, translation, buffer)
        return this
    }

    fun scale(x: Float = 1f, y: Float = 1f, z: Float = 1f): Matrix4x4f {
        val scale = floatArrayOf(
            x, 0f, 0f, 0f,
            0f, y, 0f, 0f,
            0f, 0f, z, 0f,
            0f, 0f, 0f, 1f,
        )
        compose(buffer, scale, buffer)
        return this
    }

    inline fun transform(vec: Vector3f): Vector3f {
        return times(vec)
    }

    operator fun times(other: Matrix4x4f): Matrix4x4f {
        val result = FloatArray(4 * 4)
        compose(buffer, other.buffer, result)
        return Matrix4x4f(result)
    }

    operator fun times(vec: Vector3f): Vector3f {
        val x = vec.x
        val y = vec.y
        val z = vec.z
        val w = 1f

        val ix = buffer[0]
        val iy = buffer[1]
        val iz = buffer[2]
        //val iw = buffer[3]
        val jx = buffer[4]
        val jy = buffer[5]
        val jz = buffer[6]
        //val jw = buffer[7]
        val kx = buffer[8]
        val ky = buffer[9]
        val kz = buffer[10]
        //val kw = buffer[11]
        val lx = buffer[12]
        val ly = buffer[13]
        val lz = buffer[14]
        //val lw = buffer[15]

        // (x * [ix, iy, iz, iw]) + (y * [jx, jy, jz, jw]) + (z * [kx, ky, kz, kw]) + (w * [lx, ly, lz, lw])
        val tx = (x * ix) + (y * jx) + (z * kx) + (w * lx)
        val ty = (x * iy) + (y * jy) + (z * ky) + (w * ly)
        val tz = (x * iz) + (y * jz) + (z * kz) + (w * lz)
        //val tw = (x * iz) + (y * jz) + (z * kz) + (w * lw)


        return Vector3f(tx, ty, tz)
    }

    operator fun get(idx: Int): Float = buffer[idx]

    override fun toString(): String {
        return """
            [${get(0)}, ${get(4)}, ${get(8)}, ${get(12)}]
            [${get(1)}, ${get(5)}, ${get(9)}, ${get(13)}]
            [${get(2)}, ${get(6)}, ${get(10)}, ${get(14)}]
            [${get(3)}, ${get(7)}, ${get(11)}, ${get(15)}]
        """.trimIndent()
    }

    companion object {
        // dest = a * b
        private inline fun compose(a: FloatArray, b: FloatArray, dest: FloatArray) {
            val ix = b[0]
            val iy = b[1]
            val iz = b[2]
            val iw = b[3]
            val jx = b[4]
            val jy = b[5]
            val jz = b[6]
            val jw = b[7]
            val kx = b[8]
            val ky = b[9]
            val kz = b[10]
            val kw = b[11]
            val lx = b[12]
            val ly = b[13]
            val lz = b[14]
            val lw = b[15]

            val xx = a[0]
            val xy = a[1]
            val xz = a[2]
            val xw = a[3]
            val yx = a[4]
            val yy = a[5]
            val yz = a[6]
            val yw = a[7]
            val zx = a[8]
            val zy = a[9]
            val zz = a[10]
            val zw = a[11]
            val wx = a[12]
            val wy = a[13]
            val wz = a[14]
            val ww = a[15]

            // (ix * [xx, xy, xz, xw]) + (iy * [yx, yy, yz, yw]) + (iz * [zx, zy, zz, zw]) + (iw * [wx, wy, wz, ww])
            val txx = (ix * xx) + (iy * yx) + (iz * zx) + (iw * wx)
            val txy = (ix * xy) + (iy * yy) + (iz * zy) + (iw * wy)
            val txz = (ix * xz) + (iy * yz) + (iz * zz) + (iw * wz)
            val txw = (ix * xw) + (iy * yw) + (iz * zw) + (iw * ww)

            // (jx * [xx, xy, xz, xw]) + (jy * [yx, yy, yz, yw]) + (jz * [zx, zy, zz, zw]) + (jw * [wx, wy, wz, ww])
            val tyx = (jx * xx) + (jy * yx) + (jz * zx) + (jw * wx)
            val tyy = (jx * xy) + (jy * yy) + (jz * zy) + (jw * wy)
            val tyz = (jx * xz) + (jy * yz) + (jz * zz) + (jw * wz)
            val tyw = (jx * xw) + (jy * yw) + (jz * zw) + (jw * ww)

            // (kx * [xx, xy, xz, xw]) + (ky * [yx, yy, yz, yw]) + (kz * [zx, zy, zz, zw]) + (kw * [wx, wy, wz, ww])
            val tzx = (kx * xx) + (ky * yx) + (kz * zx) + (kw * wx)
            val tzy = (kx * xy) + (ky * yy) + (kz * zy) + (kw * wy)
            val tzz = (kx * xz) + (ky * yz) + (kz * zz) + (kw * wz)
            val tzw = (kx * xz) + (ky * yz) + (kz * zw) + (kw * ww)

            // (lx * [xx, xy, xz, xw]) + (ly * [yx, yy, yz, yw]) + (lz * [zx, zy, zz, zw]) + (lw * [wx, wy, wz, ww])
            val twx = (lx * xx) + (ly * yx) + (lz * zx) + (lw * wx)
            val twy = (lx * xy) + (ly * yy) + (lz * zy) + (lw * wy)
            val twz = (lx * xz) + (ly * yz) + (lz * zz) + (lw * wz)
            val tww = (lx * xz) + (ly * yz) + (lz * zw) + (lw * ww)

            dest[0] = txx
            dest[1] = txy
            dest[2] = txz
            dest[3] = txw
            dest[4] = tyx
            dest[5] = tyy
            dest[6] = tyz
            dest[7] = tyw
            dest[8] = tzx
            dest[9] = tzy
            dest[10] = tzz
            dest[11] = tzw
            dest[12] = twx
            dest[13] = twy
            dest[14] = twz
            dest[15] = tww
        }
    }
}