package calamansi.gfx

data class Color(val argb: UInt) {
    constructor(r: Int = 0, g: Int = 0, b: Int = 0, a: Int = 255) : this(argb(r, g, b, a))
    // format: aarrggbb
    constructor(color: String) : this(argb(color))

    val a: Int = ((0xff000000u and argb) shr 24).toInt()
    val r: Int = ((0x00ff0000u and argb) shr 16).toInt()
    val g: Int = ((0x0000ff00u and argb) shr 8).toInt()
    val b: Int = ((0x000000ffu and argb)).toInt()

    fun toHexString(): String {
        return argb.toString(16)
    }

    companion object {
        val TRANSPARENT = Color(a = 0)
        val WHITE = Color(255, 255, 255)
        val GRAY = Color(125, 125, 125)
        val BLACK = Color()
        val RED = Color(r = 255)
        val GREEN = Color(g = 255)
        val BLUE = Color(b = 255)
        val CYAN = Color(g = 255, b = 255)
        val MAGENTA = Color(r = 255, b = 255)
        val YELLOW = Color(r = 255, g = 255)


        private fun argb(hex: String): UInt {
            check(hex.length == 8)
            return hex.lowercase().toUInt(16)
        }

        private fun argb(r: Int, g: Int, b: Int, a: Int): UInt {
            check(r in 0..255)
            check(g in 0..255)
            check(b in 0..255)
            check(a in 0..255)

            var argb = 0u
            argb = argb or (0xff000000u and (a shl 24).toUInt())
            argb = argb or (0x00ff0000u and (r shl 16).toUInt())
            argb = argb or (0x0000ff00u and (g shl 8).toUInt())
            argb = argb or (0x000000ffu and b.toUInt())
            return argb
        }
    }
}