package calamansi.component

import calamansi.resource.Mesh
import calamansi.resource.ResourceRef

class Renderable : Component {
    var mesh: ResourceRef<out Mesh>? = null
}