package calamansi.component

import calamansi.Component
import calamansi.Property
import org.joml.Vector3f

class Spatial : Component {
    @Property
    var translation = Vector3f()

    @Property
    var rotation = Vector3f()

    @Property
    var scale = Vector3f(1f, 1f, 1f)
}