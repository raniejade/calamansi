package calamansi

import calamansi.component.Component
import kotlin.reflect.KClass

abstract class Node {
    context(ExecutionContext) abstract fun <T : Component> addComponent(component: KClass<T>): T
    context(ExecutionContext) abstract fun <T : Component> getComponent(component: KClass<T>): T
    context(ExecutionContext) abstract fun <T : Component> hasComponent(component: KClass<T>): Boolean
    context(ExecutionContext) abstract fun <T : Component> removeComponent(component: KClass<T>): Boolean

    // Do not use! inline method with context receiver is bugged as of 1.6.20 (https://youtrack.jetbrains.com/issue/KT-52027)
    context(ExecutionContext) inline fun <reified T : Component> addComponent(): T = addComponent(T::class)
    context(ExecutionContext) inline fun <reified T : Component> getComponent(): T = getComponent(T::class)
    context(ExecutionContext) inline fun <reified T : Component> hasComponent(): Boolean = hasComponent(T::class)
    context(ExecutionContext) inline fun <reified T : Component> removeComponent(): Boolean = removeComponent(T::class)

    context(ExecutionContext) abstract var name: String
    context(ExecutionContext) abstract var parent: Node?

    context(ExecutionContext) abstract fun addChild(node: Node): Boolean
    context(ExecutionContext) abstract fun removeChild(node: Node): Boolean
    context(ExecutionContext) abstract fun getChildren(): List<Node>
}