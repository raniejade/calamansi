package calamansi.runtime.registry

import calamansi.component.Component
import kotlin.reflect.KClass

interface ComponentDefinition<T : Component> {
    val type: KClass<T>
    val placeholderInstance: T
    val dependencies: List<ComponentDefinition<*>>
}

class C : Component
object S : ComponentDefinition<C> {
    override val type = C::class
    override val placeholderInstance = C()
    override val dependencies = emptyList<ComponentDefinition<*>>()

}