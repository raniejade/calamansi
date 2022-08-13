package calamansi.logging

interface Logger {
    fun debug(msg: () -> String)
    fun info(msg : () -> String)
    fun warn(throwable: Throwable? = null, msg: () -> String)
    fun error(throwable: Throwable? = null, msg: () -> String)
}