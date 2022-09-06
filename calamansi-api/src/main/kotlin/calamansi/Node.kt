package calamansi

import calamansi.component.Component
import org.joml.Matrix4f
import org.joml.Vector3fc
import kotlin.reflect.KClass

/**
 * Node transformations operate with respect to the parent node.
 */
interface Node {
    var name: String
    fun translate(translation: Vector3fc)
    fun rotate(rotation: Vector3fc)
    fun scale(scale: Vector3fc)

    fun setTranslation(translation: Vector3fc)
    fun setRotation(rotation: Vector3fc)
    fun setScale(scale: Vector3fc)

    fun getTranslation(): Vector3fc
    fun getRotation(): Vector3fc
    fun getScale(): Vector3fc

    /**
     * Get this node's transform with respect to its parent.
     */
    fun getTransform(): Matrix4f

    /**
     * Get this node's global space transform.
     */
    fun getGlobalTransform(): Matrix4f

    var parent: Node?

    fun addComponent(component: Component)
    fun <T : Component> removeComponent(type: KClass<T>)
    fun <T : Component> containsComponent(type: KClass<T>): Boolean

    fun addChild(child: Node)
    fun removeChild(child: Node)
    fun containsChild(child: Node)
    fun getChildren(): List<Node>
}

inline fun <reified T : Component> Node.removeComponent() = removeComponent(T::class)
inline fun <reified T : Component> Node.containsComponent() = containsComponent(T::class)