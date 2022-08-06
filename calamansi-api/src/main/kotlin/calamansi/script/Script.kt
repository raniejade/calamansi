package calamansi.script

abstract class Script {
    open fun attached() {}
    open fun detached() {}
    open fun update(delta: Float) {}
}