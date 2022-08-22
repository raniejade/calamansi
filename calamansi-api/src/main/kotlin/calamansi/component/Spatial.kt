package calamansi.component

import calamansi.Component
import calamansi.Property
import calamansi.math.Vector3f

class Spatial : Component {
    @Property
    var translation: Vector3f = Vector3f()

    @Property
    var scale: Vector3f = Vector3f()

    fun translate(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        translation.x += x
        translation.y += y
        translation.z += z
    }

    fun scale(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        translation.x += x
        translation.y += y
        translation.z += z
    }
}