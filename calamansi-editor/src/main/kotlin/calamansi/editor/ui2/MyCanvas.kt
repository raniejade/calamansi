package calamansi.editor.ui2

import calamansi.ui2.control.*

class MyCanvas : Canvas() {
    init {
        val helloWorld = text("Hello World!")
        val lipsum =
            text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sodales ut felis nec commodo. Duis ullamcorper orci a rhoncus semper. Proin vitae consectetur nibh. Pellentesque blandit leo eu semper commodo. Proin lacinia nibh est, id commodo urna viverra quis. Aliquam nulla velit, suscipit nec erat nec, lobortis varius arcu. Curabitur at scelerisque elit.")
        val code = text(
            """
                    #include <iostream>
                    int main() {
                        std::cout << "Hello World" << std::endl;
                        return 0;
                    }
                """.trimIndent()
        )
        root = Container().apply {
            direction = FlexDirection.COLUMN
            justifyContent = FlexJustify.CENTER
            //addChild(helloWorld)
            addChild(Container().apply {
                addChild(lipsum)
            })
            addChild(code)
            addChild(Button("Hello World").apply { fontSize = 32f })
        }
    }

    private fun text(text: String) = Text(text).apply {
        fontSize = 32f
//        paddingLeft = 10f
//        paddingTop = 10f
//        paddingRight = 10f
//        paddingBottom = 10f
    }
}