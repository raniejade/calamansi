package calamansi

import calamansi.component.Component
import kotlin.reflect.KClass

abstract class Node {
    abstract fun <T : Component> addComponent(component: KClass<T>): T
    abstract fun <T : Component> getComponent(component: KClass<T>): T
    abstract fun <T : Component> hasComponent(component: KClass<T>): Boolean
    abstract fun <T : Component> removeComponent(component: KClass<T>): Boolean

    inline fun <reified T : Component> addComponent(): T = addComponent(T::class)
    inline fun <reified T : Component> getComponent(): T = getComponent(T::class)
    inline fun <reified T : Component> hasComponent(): Boolean = hasComponent(T::class)
    inline fun <reified T : Component> removeComponent(): Boolean = removeComponent(T::class)

    abstract var name: String
    abstract val parent: Node?
    // TODO: what is the use case?
    @Deprecated("no defined use case yet")
    abstract val script: Script?
    abstract fun hasScript(): Boolean

    abstract fun addChild(node: Node): Boolean
    abstract fun removeChild(node: Node): Boolean
    abstract fun getChildren(): List<Node>

    abstract fun hasChildren(): Boolean
}