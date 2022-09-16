package calamansi.node

import calamansi.resource.Resource

interface Scene : Resource {
    /**
     * Instantiate this scene, returning the root node.
     *
     * Returns null if the scene is empty.
     */
    fun instantiate(): Node?
}