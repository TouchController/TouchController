package top.fifthlight.combine.backend.minecraft.keycode.v26_3

import org.lwjgl.sdl.SDLKeycode
import top.fifthlight.combine.core.input.key.Key
import top.fifthlight.combine.core.input.key.KeyModifier

fun mapKeyCode(code: Int) = when (code) {
    SDLKeycode.SDLK_BACKSPACE -> Key.BACKSPACE
    SDLKeycode.SDLK_RETURN -> Key.ENTER
    SDLKeycode.SDLK_HOME -> Key.HOME
    SDLKeycode.SDLK_END -> Key.END
    SDLKeycode.SDLK_PAGEUP -> Key.PAGE_UP
    SDLKeycode.SDLK_PAGEDOWN -> Key.PAGE_DOWN
    SDLKeycode.SDLK_DELETE -> Key.DELETE
    SDLKeycode.SDLK_LEFT -> Key.ARROW_LEFT
    SDLKeycode.SDLK_UP -> Key.ARROW_UP
    SDLKeycode.SDLK_RIGHT -> Key.ARROW_RIGHT
    SDLKeycode.SDLK_DOWN -> Key.ARROW_DOWN
    SDLKeycode.SDLK_A -> Key.A
    SDLKeycode.SDLK_B -> Key.B
    SDLKeycode.SDLK_C -> Key.C
    SDLKeycode.SDLK_D -> Key.D
    SDLKeycode.SDLK_E -> Key.E
    SDLKeycode.SDLK_F -> Key.F
    SDLKeycode.SDLK_G -> Key.G
    SDLKeycode.SDLK_H -> Key.H
    SDLKeycode.SDLK_I -> Key.I
    SDLKeycode.SDLK_J -> Key.J
    SDLKeycode.SDLK_K -> Key.K
    SDLKeycode.SDLK_L -> Key.L
    SDLKeycode.SDLK_M -> Key.M
    SDLKeycode.SDLK_N -> Key.N
    SDLKeycode.SDLK_O -> Key.O
    SDLKeycode.SDLK_P -> Key.P
    SDLKeycode.SDLK_Q -> Key.Q
    SDLKeycode.SDLK_R -> Key.R
    SDLKeycode.SDLK_S -> Key.S
    SDLKeycode.SDLK_T -> Key.T
    SDLKeycode.SDLK_U -> Key.U
    SDLKeycode.SDLK_V -> Key.V
    SDLKeycode.SDLK_W -> Key.W
    SDLKeycode.SDLK_X -> Key.X
    SDLKeycode.SDLK_Y -> Key.Y
    SDLKeycode.SDLK_Z -> Key.Z
    SDLKeycode.SDLK_0 -> Key.NUM_0
    SDLKeycode.SDLK_1 -> Key.NUM_1
    SDLKeycode.SDLK_2 -> Key.NUM_2
    SDLKeycode.SDLK_3 -> Key.NUM_3
    SDLKeycode.SDLK_4 -> Key.NUM_4
    SDLKeycode.SDLK_5 -> Key.NUM_5
    SDLKeycode.SDLK_6 -> Key.NUM_6
    SDLKeycode.SDLK_7 -> Key.NUM_7
    SDLKeycode.SDLK_8 -> Key.NUM_8
    SDLKeycode.SDLK_9 -> Key.NUM_9
    else -> Key.UNKNOWN
}

fun mapModifier(code: Int) = KeyModifier(
    shift = (code and SDLKeycode.SDL_KMOD_SHIFT) != 0,
    control = (code and SDLKeycode.SDL_KMOD_CTRL) != 0,
    meta = (code and SDLKeycode.SDL_KMOD_ALT) != 0,
)
