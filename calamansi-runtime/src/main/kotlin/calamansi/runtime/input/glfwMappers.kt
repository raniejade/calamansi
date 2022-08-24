package calamansi.runtime.input

import calamansi.input.InputModifier
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