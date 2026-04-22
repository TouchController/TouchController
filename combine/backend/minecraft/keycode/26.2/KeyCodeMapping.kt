package top.fifthlight.combine.backend.minecraft.keycode.v26_2

import org.lwjgl.glfw.GLFW
import top.fifthlight.combine.input.key.Key
import top.fifthlight.combine.input.key.KeyModifier

fun mapKeyCode(code: Int) = when (code) {
    GLFW.GLFW_KEY_BACKSPACE -> Key.BACKSPACE
    GLFW.GLFW_KEY_ENTER -> Key.ENTER
    GLFW.GLFW_KEY_HOME -> Key.HOME
    GLFW.GLFW_KEY_END -> Key.END
    GLFW.GLFW_KEY_PAGE_UP -> Key.PAGE_UP
    GLFW.GLFW_KEY_PAGE_DOWN -> Key.PAGE_DOWN
    GLFW.GLFW_KEY_DELETE -> Key.DELETE
    GLFW.GLFW_KEY_LEFT -> Key.ARROW_LEFT
    GLFW.GLFW_KEY_UP -> Key.ARROW_UP
    GLFW.GLFW_KEY_RIGHT -> Key.ARROW_RIGHT
    GLFW.GLFW_KEY_DOWN -> Key.ARROW_DOWN
    GLFW.GLFW_KEY_A -> Key.A
    GLFW.GLFW_KEY_B -> Key.B
    GLFW.GLFW_KEY_C -> Key.C
    GLFW.GLFW_KEY_D -> Key.D
    GLFW.GLFW_KEY_E -> Key.E
    GLFW.GLFW_KEY_F -> Key.F
    GLFW.GLFW_KEY_G -> Key.G
    GLFW.GLFW_KEY_H -> Key.H
    GLFW.GLFW_KEY_I -> Key.I
    GLFW.GLFW_KEY_J -> Key.J
    GLFW.GLFW_KEY_K -> Key.K
    GLFW.GLFW_KEY_L -> Key.L
    GLFW.GLFW_KEY_M -> Key.M
    GLFW.GLFW_KEY_N -> Key.N
    GLFW.GLFW_KEY_O -> Key.O
    GLFW.GLFW_KEY_P -> Key.P
    GLFW.GLFW_KEY_Q -> Key.Q
    GLFW.GLFW_KEY_R -> Key.R
    GLFW.GLFW_KEY_S -> Key.S
    GLFW.GLFW_KEY_T -> Key.T
    GLFW.GLFW_KEY_U -> Key.U
    GLFW.GLFW_KEY_V -> Key.V
    GLFW.GLFW_KEY_W -> Key.W
    GLFW.GLFW_KEY_X -> Key.X
    GLFW.GLFW_KEY_Y -> Key.Y
    GLFW.GLFW_KEY_Z -> Key.Z
    GLFW.GLFW_KEY_0 -> Key.NUM_0
    GLFW.GLFW_KEY_1 -> Key.NUM_1
    GLFW.GLFW_KEY_2 -> Key.NUM_2
    GLFW.GLFW_KEY_3 -> Key.NUM_3
    GLFW.GLFW_KEY_4 -> Key.NUM_4
    GLFW.GLFW_KEY_5 -> Key.NUM_5
    GLFW.GLFW_KEY_6 -> Key.NUM_6
    GLFW.GLFW_KEY_7 -> Key.NUM_7
    GLFW.GLFW_KEY_8 -> Key.NUM_8
    GLFW.GLFW_KEY_9 -> Key.NUM_9
    else -> Key.UNKNOWN
}

fun mapModifier(code: Int) = KeyModifier(
    shift = (code and GLFW.GLFW_MOD_SHIFT) != 0,
    control = (code and GLFW.GLFW_MOD_CONTROL) != 0,
    meta = (code and GLFW.GLFW_MOD_ALT) != 0,
)
