package calamansi.node

import calamansi.resource.Resource

class Scene internal constructor(private val factory: () -> Node?) : Resource {
    fun instance(): Node? {
        return factory()
    }
}