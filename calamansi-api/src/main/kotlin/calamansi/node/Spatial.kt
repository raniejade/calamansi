package calamansi.node

import calamansi.meta.Property
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.*

open class Spatial : Node() {
    @Property
    var translation: Vector3f = Vector3f()

    @Property
    var rotation: Vector3f = Vector3f()

    @Property
    var scale: Vector3f = Vector3f()

    private var transformHash: Int? = null
    private var _transform = Matrix4f()
    private var _globalTransform = Matrix4f()

    val transform: Matrix4fc
        get() {
            computeIfDirty()
            return _transform
        }

    val globalTransform: Matrix4fc
        get() {
            computeIfDirty()
            return _globalTransform
        }


    fun translate(translation: Vector3fc) {
        this.translation.add(translation)
    }

    fun rotate(rotation: Vector3fc) {
        this.rotation.add(rotation)
    }

    fun scale(scale: Vector3fc) {
        this.scale.add(scale)
    }

    private fun computeIfDirty() {
        val hash = Objects.hash(translation, rotation, scale)
        if (transformHash == null || transformHash != hash) {
            _transform = Matrix4f()
                .rotateZ(Math.toRadians(rotation.z))
                .rotateY(Math.toRadians(rotation.y))
                .rotateX(Math.toRadians(rotation.x))
                .translate(translation)
                .scale(scale)

            val parentTransform = if (parent is Spatial) {
                (parent as Spatial).globalTransform
            } else {
                Matrix4f()
            }

            _globalTransform = Matrix4f()
                .mul(parentTransform)
                .mul(_transform)

            transformHash = hash
        }
    }
}