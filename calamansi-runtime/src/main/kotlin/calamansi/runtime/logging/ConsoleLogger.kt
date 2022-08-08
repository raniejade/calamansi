package calamansi.runtime.logging

class ConsoleLogger(logLevel: LogLevel): AbstractLogger(logLevel) {
    override fun debug(msg: String) {
        println(msg)
    }

    override fun info(msg: String) {
        println(msg)
    }

    override fun warn(msg: String) {
        println(msg)
    }

    override fun error(msg: String) {
        println(msg)
    }
}