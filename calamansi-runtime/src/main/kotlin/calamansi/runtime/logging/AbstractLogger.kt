package calamansi.runtime.logging

import calamansi.logging.Logger

abstract class AbstractLogger(private val logLevel: LogLevel) : Logger {
    override fun debug(msg: () -> String) {
        if (!canLog(LogLevel.DEBUG)) {
            return
        }
        debug(format(msg(), LogLevel.DEBUG))
    }

    override fun info(msg: () -> String) {
        if (!canLog(LogLevel.INFO)) {
            return
        }
        info(format(msg(), LogLevel.INFO))
    }

    override fun warn(msg: () -> String) {
        if (!canLog(LogLevel.WARN)) {
            return
        }
        warn(format(msg(), LogLevel.WARN))
    }

    override fun error(msg: () -> String) {
        if (!canLog(LogLevel.ERROR)) {
            return
        }
        error(format(msg(), LogLevel.ERROR))
    }

    protected abstract fun debug(msg: String)
    protected abstract fun info(msg: String)
    protected abstract fun warn(msg: String)
    protected abstract fun error(msg: String)

    private fun canLog(logLevel: LogLevel): Boolean {
        return this.logLevel.level >= logLevel.level
    }

    private fun format(msg: String, level: LogLevel): String {
        val prefix = "[$level]".padEnd(5 /* length of DEBUG and ERROR */ + 2 /* [ ] characters */)
        return "$prefix $msg"
    }
}