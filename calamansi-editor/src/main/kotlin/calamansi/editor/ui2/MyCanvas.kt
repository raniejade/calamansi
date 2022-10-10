package calamansi.editor.ui2

import calamansi.ui2.control.Canvas
import calamansi.ui2.control.Text
import calamansi.ui2.control.px
import calamansi.ui2.flex.FlexContainer
import calamansi.ui2.flex.FlexJustify

class MyCanvas : Canvas() {
    init {
        root = FlexContainer().apply {
            justifyContent = FlexJustify.CENTER
            children.addAll(listOf(
                text("Hello World!"),
                text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sodales ut felis nec commodo. Duis ullamcorper orci a rhoncus semper. Proin vitae consectetur nibh. Pellentesque blandit leo eu semper commodo. Proin lacinia nibh est, id commodo urna viverra quis. Aliquam nulla velit, suscipit nec erat nec, lobortis varius arcu. Curabitur at scelerisque elit."),
                text("""
                    #include <iostream>
                    
                    int main() {
                        std::cout << "Hello World" << std::endl;
                        return 0;
                    }
                """.trimIndent())
            ))
        }
    }

    private fun text(text: String) = Text(text).apply {
        fontSize = 32f
        paddingLeft = 10f
        paddingTop = 10f
        paddingRight = 10f
        paddingBottom = 10f

        width = 300.px
    }
}