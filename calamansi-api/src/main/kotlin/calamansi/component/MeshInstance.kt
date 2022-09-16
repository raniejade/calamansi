package calamansi.component

import calamansi.resource.Mesh
import calamansi.resource.ResourceRef

class MeshInstance : Component {
    var mesh: ResourceRef<out Mesh>? = null
}