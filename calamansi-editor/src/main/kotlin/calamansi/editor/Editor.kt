package calamansi.editor

import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.KeyStateEvent
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.resource.loadResource
import org.slf4j.LoggerFactory

class Editor : Node() {
    private val logger = LoggerFactory.getLogger(Editor::class.java)

    context(ExecutionContext) override fun onEnterTree() {
        logger.info("Enter tree.")
    }

    context(ExecutionContext) override fun onExitTree() {
        logger.info("Exit tree.")
    }

    context(ExecutionContext) override fun onEvent(event: Event) {
        logger.info("Received event: $event")
        if (event is KeyStateEvent && event.state == InputState.RELEASED && event.key == Key.A) {
            setScene(loadResource("assets://empty.scn.json"))
        }
    }

    context(ExecutionContext) override fun onUpdate(delta: Long) {
        if (isKeyPressed(Key.ESCAPE)) {
            exit()
        }
    }
}