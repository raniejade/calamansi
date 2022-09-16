package calamansi.editor

import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.KeyStateEvent
import calamansi.node.ExecutionContext
import calamansi.node.Node

class Editor : Node() {
    context(ExecutionContext) override fun onInit() {
        logger.info { "onInit" }
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