package calamansi.runtime

import calamansi.ExecutionContext

class SceneManager {
    private var root: NodeImpl? = null

    context(ExecutionContext) fun setCurrentScene(newRoot: NodeImpl?) {
        maybeDetachCurrentScene()
        root = newRoot
        root?.let {
            it.isSceneRoot = true
            it.executeAttachCallback()
        }
    }

    context(ExecutionContext) fun frame(delta: Float) {
        root?.executeUpdateCallback(delta)
    }

    context(ExecutionContext) private fun maybeDetachCurrentScene() {
        root?.let {
            it.executeDetachCallback()
            it.isSceneRoot = false
        }

        root = null
    }
}