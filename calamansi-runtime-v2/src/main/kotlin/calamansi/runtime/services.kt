package calamansi.runtime

import calamansi.runtime.service.Service
import kotlin.reflect.KClass

object Services {
    private val services = mutableMapOf<KClass<out Service>, Service>()

    fun <T : Service> create(provider: () -> T): T {
        val service = provider()
        require(!services.containsKey(service::class))
        services[service::class] = service
        return service
    }

    fun <T : Service> getService(type: KClass<T>): T {
        return services.getValue(type) as T
    }

    inline fun <reified T : Service> getService(): T {
        return getService(T::class)
    }

    inline fun <reified T : Service> get() = lazy { getService(T::class) }
}

