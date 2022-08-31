package calamansi.editor

import calamansi.ExecutionContext
import calamansi.Script
import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.Key

class EditorScript : Script() {
    context(ExecutionContext) override fun attached() {
        logger.info { "attached" }
    }

    context(ExecutionContext) override fun detached() {
        logger.info { "detached" }
    }

    context(ExecutionContext) override fun handleEvent(event: Event) {
        logger.info { "Received event: $event - ${Thread.currentThread()}" }
    }

    context(ExecutionContext) override fun update(delta: Float) {
        if (getKeyState(Key.ESCAPE) == InputState.PRESSED) {
            exit(0)
        }
    }
}