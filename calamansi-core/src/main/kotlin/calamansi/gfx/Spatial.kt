package calamansi.gfx

import calamansi.meta.Property
import calamansi.node.Node
import calamansi.runtime.utils.StateTracker
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f

open class Spatial : Node() {
    @Property
    var translation: Vector3f = Vector3f()

    @Property
    var rotation: Vector3f = Vector3f()

    @Property
    var scale: Vector3f = Vector3f(1f, 1f, 1f)

    private var _localTransform = Matrix4f()

    @Suppress("LeakingThis")
    private val transformState = StateTracker.create(
        this::translation,
        this::rotation,
        this::scale,
    )

    fun getLocalTransform(): Matrix4fc {
        if (transformState.isDirty()) {
            _localTransform = Matrix4f()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .translate(translation)
                .scale(scale)
        }

        return _localTransform.get(Matrix4f())
    }

    fun getGlobalTransform(): Matrix4fc {
        val parentTransform = (parent as? Spatial)?.getGlobalTransform() ?: Matrix4f()
        return parentTransform.mul(getLocalTransform(), Matrix4f())
    }
}