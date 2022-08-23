package calamansi.editor

import calamansi.ExecutionContext
import calamansi.Script

class EditorScript : Script() {
    context(ExecutionContext) override fun attached() {
        logger.info { "attached" }
    }

    context(ExecutionContext) override fun detached() {
        logger.info { "detached" }
    }

    context(ExecutionContext) override fun update(delta: Float) {
    }
}