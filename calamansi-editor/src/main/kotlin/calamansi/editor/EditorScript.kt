package calamansi.editor

import calamansi.ExecutionContext
import calamansi.Script
import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.KeyStateEvent

class EditorScript : Script() {
    context(ExecutionContext) override suspend fun attached() {
        logger.info { "attached" }
    }

    context(ExecutionContext) override suspend fun detached() {
        logger.info { "detached" }
    }

    context(ExecutionContext) override suspend fun handleEvent(event: Event) {
        logger.info { "Received event: $event" }
        if (event is KeyStateEvent && event.state == InputState.RELEASED && event.key == Key.A) {
            setCurrentScene(fetchResource("assets://empty.scn"))
        }
    }

    context(ExecutionContext) override suspend fun update(delta: Float) {
        if (getKeyState(Key.ESCAPE) == InputState.PRESSED) {
            exit(0)
        }
    }
}