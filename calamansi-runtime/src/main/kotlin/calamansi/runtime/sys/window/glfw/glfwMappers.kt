package calamansi.runtime.sys.window.glfw

import calamansi.input.InputModifier
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.MouseButton
import org.lwjgl.glfw.GLFW.*
import java.lang.AssertionError

object InputModifierMapper {
    fun fromGlfwModifier(mods: Int): Set<InputModifier> {
        val modifiers = mutableSetOf<InputModifier>()
        if (mods and GLFW_MOD_CONTROL != 0) {
            modifiers.add(InputModifier.CONTROL)
        }
        if (mods and GLFW_MOD_ALT != 0) {
            modifiers.add(InputModifier.ALT)
        }
        if (mods and GLFW_MOD_SHIFT != 0) {
            modifiers.add(InputModifier.SHIFT)
        }
        if (mods and GLFW_MOD_SUPER != 0) {
            modifiers.add(InputModifier.SUPER)
        }
        if (mods and GLFW_MOD_CAPS_LOCK != 0) {
            modifiers.add(InputModifier.CAPS_LOCK)
        }
        if (mods and GLFW_MOD_NUM_LOCK != 0) {
            modifiers.add(InputModifier.NUM_LOCK)
        }

        return modifiers.toSet()
    }

    fun toGlfwModifier(modifiers: Set<InputModifier>): Int {
        var mods = 0
        modifiers.forEach { modifier ->
            mods = when (modifier) {
                InputModifier.SHIFT -> mods or GLFW_MOD_SHIFT
                InputModifier.CONTROL -> mods or GLFW_MOD_CONTROL
                InputModifier.ALT -> mods or GLFW_MOD_ALT
                InputModifier.SUPER -> mods or GLFW_MOD_SUPER
                InputModifier.CAPS_LOCK -> mods or GLFW_MOD_CAPS_LOCK
                InputModifier.NUM_LOCK -> mods or GLFW_MOD_NUM_LOCK
            }
        }

        return mods
    }
}

object MouseButtonMapper {
    fun fromGlfwMouseButton(button: Int): MouseButton {
        return when (button) {
            GLFW_MOUSE_BUTTON_1 -> MouseButton.BUTTON_1
            GLFW_MOUSE_BUTTON_2 -> MouseButton.BUTTON_2
            GLFW_MOUSE_BUTTON_3 -> MouseButton.BUTTON_3
            GLFW_MOUSE_BUTTON_4 -> MouseButton.BUTTON_4
            GLFW_MOUSE_BUTTON_5 -> MouseButton.BUTTON_5
            GLFW_MOUSE_BUTTON_6 -> MouseButton.BUTTON_6
            GLFW_MOUSE_BUTTON_7 -> MouseButton.BUTTON_7
            GLFW_MOUSE_BUTTON_8 -> MouseButton.BUTTON_8
            else -> MouseButton.UNKNOWN
        }
    }

    fun toGlfwButton(button: MouseButton): Int {
        return when (button) {
            MouseButton.UNKNOWN -> throw AssertionError("Unsupported button: $button")
            MouseButton.BUTTON_1 -> GLFW_MOUSE_BUTTON_1
            MouseButton.BUTTON_2 -> GLFW_MOUSE_BUTTON_2
            MouseButton.BUTTON_3 -> GLFW_MOUSE_BUTTON_3
            MouseButton.BUTTON_4 -> GLFW_MOUSE_BUTTON_4
            MouseButton.BUTTON_5 -> GLFW_MOUSE_BUTTON_5
            MouseButton.BUTTON_6 -> GLFW_MOUSE_BUTTON_6
            MouseButton.BUTTON_7 -> GLFW_MOUSE_BUTTON_7
            MouseButton.BUTTON_8 -> GLFW_MOUSE_BUTTON_8
        }
    }
}

object InputStateMapper {
    fun fromGlfwState(state: Int): InputState {
        return when (state) {
            GLFW_PRESS -> InputState.PRESSED
            GLFW_RELEASE -> InputState.RELEASED
            GLFW_REPEAT -> InputState.PRESSED
            else -> throw AssertionError("Unsupported state: $state")
        }
    }

    fun toGlfwState(state: InputState): Int {
        return when (state) {
            InputState.PRESSED -> GLFW_PRESS
            InputState.RELEASED -> GLFW_RELEASE
        }
    }
}

object KeyMapper {
    fun fromGlfwKey(key: Int): Key {
        return when (key) {
            GLFW_KEY_SPACE -> Key.SPACE
            GLFW_KEY_APOSTROPHE -> Key.APOSTROPHE
            GLFW_KEY_COMMA -> Key.COMMA
            GLFW_KEY_MINUS -> Key.MINUS
            GLFW_KEY_PERIOD -> Key.PERIOD
            GLFW_KEY_SLASH -> Key.SLASH
            GLFW_KEY_0 -> Key.NUM_0
            GLFW_KEY_1 -> Key.NUM_1
            GLFW_KEY_2 -> Key.NUM_2
            GLFW_KEY_3 -> Key.NUM_3
            GLFW_KEY_4 -> Key.NUM_4
            GLFW_KEY_5 -> Key.NUM_5
            GLFW_KEY_6 -> Key.NUM_6
            GLFW_KEY_7 -> Key.NUM_7
            GLFW_KEY_8 -> Key.NUM_8
            GLFW_KEY_9 -> Key.NUM_9
            GLFW_KEY_SEMICOLON -> Key.SEMICOLON
            GLFW_KEY_EQUAL -> Key.EQUAL
            GLFW_KEY_A -> Key.A
            GLFW_KEY_B -> Key.B
            GLFW_KEY_C -> Key.C
            GLFW_KEY_D -> Key.D
            GLFW_KEY_E -> Key.E
            GLFW_KEY_F -> Key.F
            GLFW_KEY_G -> Key.G
            GLFW_KEY_H -> Key.H
            GLFW_KEY_I -> Key.I
            GLFW_KEY_J -> Key.J
            GLFW_KEY_K -> Key.K
            GLFW_KEY_L -> Key.L
            GLFW_KEY_M -> Key.M
            GLFW_KEY_N -> Key.N
            GLFW_KEY_O -> Key.O
            GLFW_KEY_P -> Key.P
            GLFW_KEY_Q -> Key.Q
            GLFW_KEY_R -> Key.R
            GLFW_KEY_S -> Key.S
            GLFW_KEY_T -> Key.T
            GLFW_KEY_U -> Key.U
            GLFW_KEY_V -> Key.V
            GLFW_KEY_W -> Key.W
            GLFW_KEY_X -> Key.X
            GLFW_KEY_Y -> Key.Y
            GLFW_KEY_Z -> Key.Z
            GLFW_KEY_LEFT_BRACKET -> Key.LEFT_BRACKET
            GLFW_KEY_BACKSLASH -> Key.BACKSLASH
            GLFW_KEY_RIGHT_BRACKET -> Key.RIGHT_BRACKET
            GLFW_KEY_GRAVE_ACCENT -> Key.GRAVE_ACCENT
            GLFW_KEY_ESCAPE -> Key.ESCAPE
            GLFW_KEY_ENTER -> Key.ENTER
            GLFW_KEY_TAB -> Key.TAB
            GLFW_KEY_BACKSPACE -> Key.BACKSPACE
            GLFW_KEY_INSERT -> Key.INSERT
            GLFW_KEY_DELETE -> Key.DELETE
            GLFW_KEY_RIGHT -> Key.RIGHT
            GLFW_KEY_LEFT -> Key.LEFT
            GLFW_KEY_DOWN -> Key.DOWN
            GLFW_KEY_UP -> Key.UP
            GLFW_KEY_PAGE_UP -> Key.PAGE_UP
            GLFW_KEY_PAGE_DOWN -> Key.PAGE_DOWN
            GLFW_KEY_HOME -> Key.HOME
            GLFW_KEY_END -> Key.END
            GLFW_KEY_CAPS_LOCK -> Key.CAPS_LOCK
            GLFW_KEY_SCROLL_LOCK -> Key.SCROLL_LOCK
            GLFW_KEY_NUM_LOCK -> Key.NUM_LOCK
            GLFW_KEY_PRINT_SCREEN -> Key.PRINT_SCREEN
            GLFW_KEY_PAUSE -> Key.PAUSE
            GLFW_KEY_F1 -> Key.F1
            GLFW_KEY_F2 -> Key.F2
            GLFW_KEY_F3 -> Key.F3
            GLFW_KEY_F4 -> Key.F4
            GLFW_KEY_F5 -> Key.F5
            GLFW_KEY_F6 -> Key.F6
            GLFW_KEY_F7 -> Key.F7
            GLFW_KEY_F8 -> Key.F8
            GLFW_KEY_F9 -> Key.F9
            GLFW_KEY_F10 -> Key.F10
            GLFW_KEY_F11 -> Key.F11
            GLFW_KEY_F12 -> Key.F12
            GLFW_KEY_KP_0 -> Key.KEYPAD_NUM_0
            GLFW_KEY_KP_1 -> Key.KEYPAD_NUM_1
            GLFW_KEY_KP_2 -> Key.KEYPAD_NUM_2
            GLFW_KEY_KP_3 -> Key.KEYPAD_NUM_3
            GLFW_KEY_KP_4 -> Key.KEYPAD_NUM_4
            GLFW_KEY_KP_5 -> Key.KEYPAD_NUM_5
            GLFW_KEY_KP_6 -> Key.KEYPAD_NUM_6
            GLFW_KEY_KP_7 -> Key.KEYPAD_NUM_7
            GLFW_KEY_KP_8 -> Key.KEYPAD_NUM_8
            GLFW_KEY_KP_9 -> Key.KEYPAD_NUM_9
            GLFW_KEY_KP_DECIMAL -> Key.KEYPAD_DECIMAL
            GLFW_KEY_KP_DIVIDE -> Key.KEYPAD_DIVIDE
            GLFW_KEY_KP_MULTIPLY -> Key.KEYPAD_MULTIPLY
            GLFW_KEY_KP_SUBTRACT -> Key.KEYPAD_SUBTRACT
            GLFW_KEY_KP_ADD -> Key.KEYPAD_ADD
            GLFW_KEY_KP_ENTER -> Key.KEYPAD_ENTER
            GLFW_KEY_KP_EQUAL -> Key.KEYPAD_EQUAL
            GLFW_KEY_LEFT_SHIFT -> Key.LEFT_SHIFT
            GLFW_KEY_LEFT_CONTROL -> Key.LEFT_CONTROL
            GLFW_KEY_LEFT_ALT -> Key.LEFT_ALT
            GLFW_KEY_LEFT_SUPER -> Key.LEFT_SUPER
            GLFW_KEY_RIGHT_SHIFT -> Key.RIGHT_SHIFT
            GLFW_KEY_RIGHT_CONTROL -> Key.RIGHT_CONTROL
            GLFW_KEY_RIGHT_ALT -> Key.RIGHT_ALT
            GLFW_KEY_RIGHT_SUPER -> Key.RIGHT_SUPER
            GLFW_KEY_MENU -> Key.MENU
            else -> throw AssertionError("Unsupported key: $key")
        }
    }

    fun toGlfwKey(key: Key): Int {
        return when (key) {
            Key.UNKNOWN -> throw AssertionError("Unsupported key: $key")
            Key.SPACE -> GLFW_KEY_SPACE
            Key.APOSTROPHE -> GLFW_KEY_APOSTROPHE
            Key.COMMA -> GLFW_KEY_COMMA
            Key.MINUS -> GLFW_KEY_MINUS
            Key.PERIOD -> GLFW_KEY_PERIOD
            Key.SLASH -> GLFW_KEY_SLASH
            Key.NUM_0 -> GLFW_KEY_0
            Key.NUM_1 -> GLFW_KEY_1
            Key.NUM_2 -> GLFW_KEY_2
            Key.NUM_3 -> GLFW_KEY_3
            Key.NUM_4 -> GLFW_KEY_4
            Key.NUM_5 -> GLFW_KEY_5
            Key.NUM_6 -> GLFW_KEY_6
            Key.NUM_7 -> GLFW_KEY_7
            Key.NUM_8 -> GLFW_KEY_8
            Key.NUM_9 -> GLFW_KEY_9
            Key.SEMICOLON -> GLFW_KEY_SEMICOLON
            Key.EQUAL -> GLFW_KEY_EQUAL
            Key.A -> GLFW_KEY_A
            Key.B -> GLFW_KEY_B
            Key.C -> GLFW_KEY_C
            Key.D -> GLFW_KEY_D
            Key.E -> GLFW_KEY_E
            Key.F -> GLFW_KEY_F
            Key.G -> GLFW_KEY_G
            Key.H -> GLFW_KEY_H
            Key.I -> GLFW_KEY_I
            Key.J -> GLFW_KEY_J
            Key.K -> GLFW_KEY_K
            Key.L -> GLFW_KEY_L
            Key.M -> GLFW_KEY_M
            Key.N -> GLFW_KEY_N
            Key.O -> GLFW_KEY_O
            Key.P -> GLFW_KEY_P
            Key.Q -> GLFW_KEY_Q
            Key.R -> GLFW_KEY_R
            Key.S -> GLFW_KEY_S
            Key.T -> GLFW_KEY_T
            Key.U -> GLFW_KEY_U
            Key.V -> GLFW_KEY_V
            Key.W -> GLFW_KEY_W
            Key.X -> GLFW_KEY_X
            Key.Y -> GLFW_KEY_Y
            Key.Z -> GLFW_KEY_Z
            Key.LEFT_BRACKET -> GLFW_KEY_LEFT_BRACKET
            Key.BACKSLASH -> GLFW_KEY_BACKSLASH
            Key.RIGHT_BRACKET -> GLFW_KEY_RIGHT_BRACKET
            Key.GRAVE_ACCENT -> GLFW_KEY_GRAVE_ACCENT
            Key.ESCAPE -> GLFW_KEY_ESCAPE
            Key.ENTER -> GLFW_KEY_ENTER
            Key.TAB -> GLFW_KEY_TAB
            Key.BACKSPACE -> GLFW_KEY_BACKSPACE
            Key.INSERT -> GLFW_KEY_INSERT
            Key.DELETE -> GLFW_KEY_DELETE
            Key.RIGHT -> GLFW_KEY_RIGHT
            Key.LEFT -> GLFW_KEY_LEFT
            Key.DOWN -> GLFW_KEY_DOWN
            Key.UP -> GLFW_KEY_UP
            Key.PAGE_UP -> GLFW_KEY_PAGE_UP
            Key.PAGE_DOWN -> GLFW_KEY_PAGE_DOWN
            Key.HOME -> GLFW_KEY_HOME
            Key.END -> GLFW_KEY_END
            Key.CAPS_LOCK -> GLFW_KEY_CAPS_LOCK
            Key.SCROLL_LOCK -> GLFW_KEY_SCROLL_LOCK
            Key.NUM_LOCK -> GLFW_KEY_NUM_LOCK
            Key.PRINT_SCREEN -> GLFW_KEY_PRINT_SCREEN
            Key.PAUSE -> GLFW_KEY_PAUSE
            Key.F1 -> GLFW_KEY_F1
            Key.F2 -> GLFW_KEY_F2
            Key.F3 -> GLFW_KEY_F3
            Key.F4 -> GLFW_KEY_F4
            Key.F5 -> GLFW_KEY_F5
            Key.F6 -> GLFW_KEY_F6
            Key.F7 -> GLFW_KEY_F7
            Key.F8 -> GLFW_KEY_F8
            Key.F9 -> GLFW_KEY_F9
            Key.F10 -> GLFW_KEY_F10
            Key.F11 -> GLFW_KEY_F11
            Key.F12 -> GLFW_KEY_F12
            Key.KEYPAD_NUM_0 -> GLFW_KEY_KP_0
            Key.KEYPAD_NUM_1 -> GLFW_KEY_KP_1
            Key.KEYPAD_NUM_2 -> GLFW_KEY_KP_2
            Key.KEYPAD_NUM_3 -> GLFW_KEY_KP_3
            Key.KEYPAD_NUM_4 -> GLFW_KEY_KP_4
            Key.KEYPAD_NUM_5 -> GLFW_KEY_KP_5
            Key.KEYPAD_NUM_6 -> GLFW_KEY_KP_6
            Key.KEYPAD_NUM_7 -> GLFW_KEY_KP_7
            Key.KEYPAD_NUM_8 -> GLFW_KEY_KP_8
            Key.KEYPAD_NUM_9 -> GLFW_KEY_KP_9
            Key.KEYPAD_DECIMAL -> GLFW_KEY_KP_DECIMAL
            Key.KEYPAD_DIVIDE -> GLFW_KEY_KP_DIVIDE
            Key.KEYPAD_MULTIPLY -> GLFW_KEY_KP_MULTIPLY
            Key.KEYPAD_SUBTRACT -> GLFW_KEY_KP_SUBTRACT
            Key.KEYPAD_ADD -> GLFW_KEY_KP_ADD
            Key.KEYPAD_ENTER -> GLFW_KEY_KP_ENTER
            Key.KEYPAD_EQUAL -> GLFW_KEY_KP_EQUAL
            Key.LEFT_SHIFT -> GLFW_KEY_LEFT_SHIFT
            Key.LEFT_CONTROL -> GLFW_KEY_LEFT_CONTROL
            Key.LEFT_ALT -> GLFW_KEY_LEFT_ALT
            Key.LEFT_SUPER -> GLFW_KEY_LEFT_SUPER
            Key.RIGHT_SHIFT -> GLFW_KEY_RIGHT_SHIFT
            Key.RIGHT_CONTROL -> GLFW_KEY_RIGHT_CONTROL
            Key.RIGHT_ALT -> GLFW_KEY_RIGHT_ALT
            Key.RIGHT_SUPER -> GLFW_KEY_RIGHT_SUPER
            Key.MENU -> GLFW_KEY_MENU
        }
    }
}