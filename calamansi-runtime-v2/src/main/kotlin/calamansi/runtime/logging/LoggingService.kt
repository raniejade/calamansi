package calamansi.runtime.logging

import calamansi.logging.Logger
import calamansi.runtime.service.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class LoggingService : Service {
    private lateinit var logLevel: LogLevel
    private val loggers = ConcurrentHashMap<KClass<*>, Logger>()

    override fun start() {
        // nada
    }

    fun configureLogging(logLevel: LogLevel) {
        this.logLevel = logLevel
    }

    fun getLogger(source: KClass<*>): Logger {
        return loggers.getOrPut(source) { ConsoleLogger(checkNotNull(source.qualifiedName), logLevel) }
    }

    override fun stop() {
        // nada
    }
}