package calamansi.runtime.module

import calamansi.logging.Logger
import calamansi.runtime.logging.ConsoleLogger
import calamansi.runtime.logging.LogLevel
import java.util.*
import kotlin.reflect.KClass

@JvmInline
value class Handle(val internal: Any)

abstract class Module {
    protected val logger: Logger by lazy {
        getLogger(this::class)
    }

    init {
        registerModule(this::class, this)
    }

    abstract fun start()
    abstract fun shutdown()

    // TODO: proper way to pass dependencies
    companion object {
        @PublishedApi
        internal val modules = mutableMapOf<KClass<*>, Module>()
        private var logLevel = LogLevel.INFO
        private val loggers = WeakHashMap<KClass<*>, Logger>()

        private fun registerModule(type: KClass<*>, module: Module) {
            modules[type] = module
        }

        inline fun <reified T : Module> getModule(): T {
            return modules[T::class] as T
        }

        fun configureLogging(logLevel: LogLevel) {
            this.logLevel = logLevel
        }

        fun getLogger(source: KClass<*>): Logger {
            return loggers.computeIfAbsent(source) {
                ConsoleLogger(checkNotNull(it.qualifiedName), logLevel)
            }
        }
    }
}