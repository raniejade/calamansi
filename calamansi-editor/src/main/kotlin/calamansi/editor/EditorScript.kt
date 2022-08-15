package calamansi.editor

import calamansi.ExecutionContext
import calamansi.Script

class EditorScript : Script() {
    private var elapsedTime = 0f

    context(ExecutionContext) override fun attached() {
        logger.info { "attached" }
    }

    context(ExecutionContext) override fun detached() {
        logger.info { "detached" }
    }

    context(ExecutionContext) override fun update(delta: Float) {
        elapsedTime += delta

        if (elapsedTime > 10) {
            logger.info { "Requesting exit." }
            exit(0)
        }
    }
}