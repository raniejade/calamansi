package calamansi.runtime

import calamansi.Scene
import calamansi.event.Event
import calamansi.resource.ResourceRef
import calamansi.runtime.data.SerializedNode
import calamansi.runtime.data.SerializedScene
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule

class SceneModule : Module() {
    private var root: NodeImpl? = null

    override fun start() {
        logger.info { "Node module started." }
    }

    fun setCurrentScene(scene: ResourceRef<Scene>) {
        maybeDetachCurrentScene()
        root = scene.get().create() as NodeImpl?
        root?.let {
            it.isSceneRoot = true
            it.attached()
        }
    }

    fun frame(delta: Float) {
        root?.let { it.update(delta) }
    }

    fun publishEvent(event: Event) {

    }

    fun unloadCurrentScene() {
        maybeDetachCurrentScene()
    }

    private fun maybeDetachCurrentScene() {
        root?.let {
            it.detached()
            it.isSceneRoot = false
        }

        root = null
    }

    override fun shutdown() {
        logger.info { "Node module shutting down." }
    }
}