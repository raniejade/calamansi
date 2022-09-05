package calamansi.editor

import calamansi.ExecutionContext
import calamansi.Script
import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.KeyStateEvent

class EditorScript : Script() {
    context(ExecutionContext) override fun onAttached() {
        logger.info { "attached" }
    }

    context(ExecutionContext) override fun onDetached() {
        logger.info { "detached" }
    }

    context(ExecutionContext) override fun onEvent(event: Event) {
        logger.info { "Received event: $event" }
        if (event is KeyStateEvent && event.state == InputState.RELEASED && event.key == Key.A) {
            setScene(loadResource("assets://empty.scn"))
        }
    }

    context(ExecutionContext) override fun onUpdate(delta: Long) {
        if (isKeyPressed(Key.ESCAPE)) {
            exit()
        }
    }
}