package calamansi.component

import calamansi.Component
import calamansi.Property
import calamansi.math.Transform3d
import calamansi.math.Vector3f

class Spatial : Component {
    @Property
    var translation = Vector3f()

    @Property
    var rotation = Vector3f()

    @Property
    var scale = Vector3f(1f, 1f, 1f)

    fun getTransform(): Transform3d {
        return Transform3d()
            .scale(scale.x, scale.y, scale.z)
            .rotateZ(rotation.z)
            .rotateY(rotation.y)
            .rotateX(rotation.x)
            .translate(translation.x, translation.y, translation.z)
    }
}