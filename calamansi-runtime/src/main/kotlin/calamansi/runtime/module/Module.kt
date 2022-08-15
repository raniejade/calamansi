package calamansi.runtime.module

import calamansi.logging.Logger
import calamansi.runtime.logging.LoggerModule
import kotlin.reflect.KClass

@JvmInline
value class Handle(val internal: Any)

abstract class Module {
    protected val logger: Logger by lazy {
        getModule<LoggerModule>().getLogger(this::class)
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

        private fun registerModule(type: KClass<*>, module: Module) {
            modules[type] = module
        }

        inline fun <reified T : Module> getModule(): T {
            return modules[T::class] as T
        }
    }
}