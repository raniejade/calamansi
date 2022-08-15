package calamansi.v2

import calamansi.component.Component
import kotlin.reflect.KClass

abstract class Node {
    abstract var name: String
    abstract var parent: Node?

    abstract fun <T : Component> addComponent(component: KClass<T>): T
    abstract fun <T : Component> getComponent(component: KClass<T>): T
    abstract fun <T : Component> removeComponent(component: KClass<T>): Boolean
    abstract fun <T : Component> hasComponent(component: KClass<T>): Boolean

    inline fun <reified T : Component> addComponent(): T = addComponent(T::class)
    inline fun <reified T : Component> getComponent(): T = getComponent(T::class)
    inline fun <reified T : Component> removeComponent(): Boolean = removeComponent(T::class)
    inline fun <reified T : Component> hasComponent(): Boolean = hasComponent(T::class)

    abstract fun addChild(child: Node): Boolean
    abstract fun removeChild(child: Node): Boolean
    abstract fun hasChild(child: Node): Boolean

    abstract fun getChildren(): List<Node>
}