package calamansi.ui

data class Box(var left: Float = 0f, var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f) {
    constructor(value: Float) : this(value, value, value, value)
}

data class Corner(var topLeft: Float = 0f, var topRight: Float = 0f, var bottomRight: Float = 0f, var bottomLeft: Float = 0f) {
    constructor(value: Float) : this(value, value, value, value)
}
