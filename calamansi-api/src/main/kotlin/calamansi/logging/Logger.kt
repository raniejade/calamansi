package calamansi.logging

interface Logger {
    fun info(msg : () -> String)
    fun warn(msg : () -> String)
    fun error(msg : () -> String)
}