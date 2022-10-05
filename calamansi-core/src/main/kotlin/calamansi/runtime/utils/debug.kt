package calamansi.runtime.utils

// TODO: move somewhere better
var isDebugDraw = false

fun debugDraw(block: () -> Unit) {
    if (isDebugDraw) {
        block()
    }
}