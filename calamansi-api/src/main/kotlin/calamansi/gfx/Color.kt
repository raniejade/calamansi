package calamansi.gfx

class Color(val r: Int, val g: Int, val b: Int, val a: Int) {
    init {
        check(r in 0..255)
        check(g in 0..255)
        check(b in 0..255)
        check(a in 0..255)
    }
}