package calamansi.gfx

data class Color(val r: Int = 0, val g: Int = 0, val b: Int = 0, val a: Int = 255) {
    init {
        check(r in 0..255)
        check(g in 0..255)
        check(b in 0..255)
        check(a in 0..255)
    }

    companion object {
        val TRANSPARENT = Color(a = 0)
        val WHITE = Color(255, 255, 255)
        val BLACK = Color()
        val RED = Color(r = 255)
        val GREEN = Color(g = 255)
        val BLUE = Color(b = 255)
        val CYAN = Color(g = 255, b = 255)
        val MAGENTA = Color(r = 255, b = 255)
        val YELLOW = Color(r = 255, g = 255)
    }
}