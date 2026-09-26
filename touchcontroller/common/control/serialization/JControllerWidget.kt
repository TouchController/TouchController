/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.serialization

import top.fifthlight.touchcontroller.common.control.joystick.serialization.JJoystick
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButton
import top.fifthlight.touchcontroller.common.control.widget.boat.serialization.JBoatButton
import top.fifthlight.touchcontroller.common.control.widget.custom.CustomWidget
import top.fifthlight.touchcontroller.common.control.widget.custom.serialization.JCustomWidget
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPad
import top.fifthlight.touchcontroller.common.control.widget.dpad.serialization.JDPad
import top.fifthlight.touchcontroller.common.control.widget.joystick.Joystick
import top.fifthlight.touchcontroller.common.serialization.sealedByName

val JControllerWidget = sealedByName {
    "boat_button" encodes subtype<BoatButton>(JBoatButton)
    "custom" encodes subtype<CustomWidget>(JCustomWidget)
    "dpad" encodes subtype<DPad>(JDPad)
    "joystick" encodes subtype<Joystick>(JJoystick)
}
