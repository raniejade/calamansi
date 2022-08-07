package calamansi.runtime.registry

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

sealed class Property<T, V>(
    val type: KClass<V & Any>, val name: String, private val internal: KMutableProperty1<T, V>
) {
    fun set(thisRef: T, value: V) {
        internal.set(thisRef, value)
    }

    fun get(thisRef: T): V {
        return internal.get(thisRef)
    }
}

class SimpleProperty<T, V>(type: KClass<V & Any>, name: String, internal: KMutableProperty1<T, V>) :
    Property<T, V>(type, name, internal)

class EnumProperty<T, V : Enum<V>>(
    type: KClass<V>, name: String, internal: KMutableProperty1<T, V>, val values: Array<V>
) : Property<T, V>(type, name, internal)