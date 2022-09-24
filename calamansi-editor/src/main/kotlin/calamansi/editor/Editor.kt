package calamansi.editor

import calamansi.event.Event
import calamansi.gfx.Color
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.KeyStateEvent
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.resource.loadResource
import calamansi.ui.FlexValue
import calamansi.ui.Text
import org.slf4j.LoggerFactory

class Editor : Node() {
    private val logger = LoggerFactory.getLogger(Editor::class.java)

    private lateinit var text: Text

    context(ExecutionContext) override fun onEnterTree() {
        logger.info("Enter tree.")
        text = Text("Hello World!").apply {
            backgroundColor = Color.BLUE
            padding.top = FlexValue.Fixed(10f)
            padding.left = FlexValue.Fixed(10f)
        }

        addChild(text)
    }

    context(ExecutionContext) override fun onExitTree() {
        logger.info("Exit tree.")
        removeChild(text)
    }

    context(ExecutionContext) override fun onEvent(event: Event) {
        if (event is KeyStateEvent && event.state == InputState.RELEASED && event.key == Key.A) {
            setScene(loadResource("assets://empty.scn.json"))
        }
    }

    context(ExecutionContext) override fun onUpdate(delta: Float) {
        if (isKeyPressed(Key.ESCAPE)) {
            exit()
        }

        text.text = String.format("Frame time: %.3fms FPS: %.0f", getFrameTime(), getFps())

        if (isKeyPressed(Key.NUM_1)) {
            text.fontSize = 12f
        } else if (isKeyPressed(Key.NUM_2)) {
            text.fontSize = 32f
        } else if (isKeyPressed(Key.NUM_3)) {
            text.fontSize = 64f
        }
    }
}