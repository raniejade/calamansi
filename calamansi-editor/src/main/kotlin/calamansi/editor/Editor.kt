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
    private lateinit var textArea: TextArea
    private lateinit var button: Button
    private var counter = 0

    context(ExecutionContext) override fun onEnterTree() {
        logger.info("Enter tree.")
        canvas.apply {
            direction = FlexDirection.COLUMN
            alignItems = FlexAlign.CENTER
            alignContent = FlexAlign.CENTER
        }
        text = Text("Hello World!").apply {
            // width = FlexValue.Fixed(100f)
        }
        textArea = TextArea("Oh yes").apply {
            maxWidth = FlexValue.Fixed(100f)
            maxHeight = FlexValue.Fixed(200f)
            padding.top = FlexValue.Fixed(2f)
            padding.bottom = FlexValue.Fixed(2f)
            padding.left = FlexValue.Fixed(2f)
            padding.right = FlexValue.Fixed(2f)

        }
        button = Button("Clicked: $counter times.").apply {
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
        addChild(textArea)
        addChild(text)
        addChild(button)
    }

    context(ExecutionContext) override fun onExitTree() {
        logger.info("Exit tree.")
        // removeChild(text)
    }

    context(ExecutionContext) override fun onUpdate(delta: Float) {
        if (isKeyPressed(Key.ESCAPE)) {
            exit()
        }

        if (isKeyPressed(Key.NUM_1)) {
            text.text =
                "Suspendisse dolor nibh, mollis id metus eget, varius malesuada elit. Sed at justo dignissim, molestie velit vitae, fermentum mi. Curabitur magna turpis, imperdiet sed nunc ac, aliquam aliquet nibh. Nulla luctus nisi nec mauris luctus porttitor. Aenean ac malesuada tellus. Pellentesque pretium erat nec vulputate convallis. Nam dignissim dignissim massa sed laoreet. Ut sagittis tortor sit amet leo rutrum, sed mollis dolor tincidunt. Aliquam vel mi orci. Donec volutpat sit amet velit quis imperdiet. Morbi non mi sit amet odio iaculis maximus et id mauris. Morbi feugiat efficitur dui a pulvinar. Donec vel suscipit lectus. Nullam ac porttitor metus. Nunc turpis leo, ultrices faucibus justo in, eleifend consequat dolor. Donec pretium lacus tortor, tincidunt vulputate libero lacinia non. "
            // text.fontSize = 12f
            // textInput.fontSize = 12f
            // button.fontSize = 12f
        } else if (isKeyPressed(Key.NUM_2)) {
            text.text = "Hello World"
            // text.fontSize = 32f
            // textInput.fontSize = 32f
            // button.fontSize = 32f
        } else if (isKeyPressed(Key.NUM_3)) {
            // text.fontSize = 64f
            // textInput.fontSize = 64f
            // button.fontSize = 64f
        }
    }
}