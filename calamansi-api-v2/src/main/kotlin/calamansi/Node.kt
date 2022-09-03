package calamansi

import calamansi.component.Component
import org.joml.Matrix4f
import org.joml.Vector3fc
import kotlin.reflect.KClass

/**
 * Node transformations operate with respect to the parent node.
 */
abstract class Node {
    abstract fun translate(translation: Vector3fc)
    abstract fun rotate(rotation: Vector3fc)
    abstract fun scale(scale: Vector3fc)

    abstract fun getTranslation(): Vector3fc
    abstract fun getRotation(): Vector3fc
    abstract fun getScale(): Vector3fc

    /**
     * Get this node's transform with respect to its parent.
     */
    abstract fun getTransform(): Matrix4f

    /**
     * Get this node's global space transform.
     */
    abstract fun getGlobalTransform(): Matrix4f

    abstract var parent: Group?

    abstract fun addComponent(component: Component)
    abstract fun <T : Component> removeComponent(type: KClass<T>)
    abstract fun <T : Component> containsComponent(type: KClass<T>): Boolean

    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)
    inline fun <reified T : Component> containsComponent() = containsComponent(T::class)
}

/**
 * A node that has children.
 */
abstract class Group : Node() {
    abstract fun addChild(child: Node)
    abstract fun removeChild(child: Node)
    abstract fun containsChild(child: Node)
}