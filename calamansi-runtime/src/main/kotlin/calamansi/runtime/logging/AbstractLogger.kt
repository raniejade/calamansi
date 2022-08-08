package calamansi.runtime.logging

import calamansi.logging.Logger

abstract class AbstractLogger(private val logLevel: LogLevel) : Logger {
    override fun debug(msg: () -> String) {
        if (!canLog(LogLevel.DEBUG)) {
            return
        }
        debug(msg())
    }

    override fun info(msg: () -> String) {
        if (!canLog(LogLevel.INFO)) {
            return
        }
        info(msg())
    }

    override fun warn(msg: () -> String) {
        if (!canLog(LogLevel.WARN)) {
            return
        }
        warn(msg())
    }

    override fun error(msg: () -> String) {
        if (!canLog(LogLevel.ERROR)) {
            return
        }
        error(msg())
    }

    protected abstract fun debug(msg: String)
    protected abstract fun info(msg: String)
    protected abstract fun warn(msg: String)
    protected abstract fun error(msg: String)

    private fun canLog(logLevel: LogLevel): Boolean {
        return this.logLevel.level >= logLevel.level
    }
}