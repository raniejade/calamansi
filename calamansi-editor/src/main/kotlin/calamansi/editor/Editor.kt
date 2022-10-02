package calamansi.editor

import calamansi.input.Key
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.resource.loadResource
import calamansi.ui.*
import org.slf4j.LoggerFactory

class Editor : Node() {
    private val logger = LoggerFactory.getLogger(Editor::class.java)

    private lateinit var text: Text
    private lateinit var textInput: TextInput
    private lateinit var button: Button
    private var counter = 0

    context(ExecutionContext) override fun onEnterTree() {
        logger.info("Enter tree.")
        canvas.apply {
            direction = FlexDirection.COLUMN_REVERSE
            alignItems = FlexAlign.CENTER
            alignContent = FlexAlign.CENTER
        }
        text = Text("Hello World!").apply {
            margin.bottom = FlexValue.Fixed(10f)
            padding.top = FlexValue.Fixed(10f)
            padding.bottom = FlexValue.Fixed(10f)
            padding.left = FlexValue.Fixed(10f)
            padding.right = FlexValue.Fixed(10f)
        }
        textInput = TextInput().apply {
            width = FlexValue.Fixed(100f)
            height = FlexValue.Fixed(20f)
        }
        button = Button("Clicked: $counter times.").apply {
            margin.bottom = FlexValue.Fixed(10f)
            padding.top = FlexValue.Fixed(10f)
            padding.bottom = FlexValue.Fixed(10f)
            padding.left = FlexValue.Fixed(10f)
            padding.right = FlexValue.Fixed(10f)

            this.subscribe { message ->
                if (message is CanvasMessage.ButtonPress) {
                    counter++
                    text = "Clicked: $counter times."
                    if (counter > 10) {
                        setScene(loadResource("assets://empty.scn.json"))
                    }
                }
            }
        }
        addChild(text)
        addChild(textInput)
        addChild(button)
    }

    context(ExecutionContext) override fun onExitTree() {
        logger.info("Exit tree.")
        removeChild(text)
    }

    context(ExecutionContext) override fun onUpdate(delta: Float) {
        if (isKeyPressed(Key.ESCAPE)) {
            exit()
        }

        text.text = String.format("Frame time: %.3fms FPS: %.0f", getFrameTime(), getFps())

        if (isKeyPressed(Key.NUM_1)) {
            text.fontSize = 12f
            button.fontSize = 12f
        } else if (isKeyPressed(Key.NUM_2)) {
            text.fontSize = 32f
            button.fontSize = 32f
        } else if (isKeyPressed(Key.NUM_3)) {
            text.fontSize = 64f
            button.fontSize = 64f
        }
    }
}