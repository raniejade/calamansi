package calamansi.runtime.logging

import calamansi.logging.Logger
import calamansi.runtime.module.Module
import kotlin.reflect.KClass

class LoggerModule : Module() {
    private var logLevel = LogLevel.INFO
    private val loggers = mutableMapOf<KClass<*>, Logger>()

    override fun start() {}

    override fun shutdown() {}

    fun configure(logLevel: LogLevel) {
        this.logLevel = logLevel
    }

    fun getLogger(source: KClass<*>): Logger {
        return loggers.computeIfAbsent(source) {
            ConsoleLogger(checkNotNull(it.qualifiedName), logLevel)
        }
    }
}