/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.gal.gamestate

import top.fifthlight.mergetools.api.ExpectFactory

interface CameraPerspective {
    enum class PredefinedCameraType {
        FIRST_PERSON,
        THIRD_PERSON_BACK,
        THIRD_PERSON_FRONT,
    }

    val predefinedType: PredefinedCameraType?

    fun cycle(): CameraPerspective

    @ExpectFactory
    interface Factory {
        fun firstPerson(): CameraPerspective
        fun thirdPersonBack(): CameraPerspective
        fun thirdPersonFront(): CameraPerspective
    }
}
