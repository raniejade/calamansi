package calamansi.editor

import calamansi.editor.ui2.MyCanvas
import calamansi.input.Key
import calamansi.node.ExecutionContext
import calamansi.node.Node
import org.slf4j.LoggerFactory

class Editor : Node() {
    private val logger = LoggerFactory.getLogger(Editor::class.java)

    private lateinit var canvas: MyCanvas

    context(ExecutionContext) override fun onEnterTree() {
        logger.info("Enter tree.")

        canvas = MyCanvas()

        addChild(canvas)
    }

    context(ExecutionContext) override fun onExitTree() {
        logger.info("Exit tree.")
    }

    context(ExecutionContext) override fun onUpdate(delta: Float) {
        if (isKeyPressed(Key.ESCAPE)) {
            exit()
        }
    }
}