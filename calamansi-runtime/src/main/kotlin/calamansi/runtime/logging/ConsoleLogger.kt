package calamansi.runtime.logging

class ConsoleLogger(logLevel: LogLevel) : AbstractLogger(logLevel) {
    override fun debug(msg: String) {
        println(msg)
    }

    override fun info(msg: String) {
        println(msg)
    }

    override fun warn(throwable: Throwable?, msg: String) {
        println(msg)
        throwable?.printStackTrace()
    }

    override fun error(throwable: Throwable?, msg: String) {
        println(msg)
        throwable?.printStackTrace()
    }
}