package calamansi.ui2.control

sealed class DimValue {
    object Undefined : DimValue()
    class Fixed(val value: Float) : DimValue()
    class Percentage(val pc: Float) : DimValue() {
        init {
            check(pc in 0f..100f) { "$pc must be between 0 and 100" }
        }
    }
}
