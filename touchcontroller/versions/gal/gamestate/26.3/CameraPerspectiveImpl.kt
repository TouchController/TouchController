/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.gamestate.v26_3

import net.minecraft.client.CameraType
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.gamestate.CameraPerspective

@ActualImpl(CameraPerspective::class)
class CameraPerspectiveImpl(private val cameraType: CameraType) : CameraPerspective {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override val predefinedType: CameraPerspective.PredefinedCameraType? = when (cameraType) {
        CameraType.FIRST_PERSON -> CameraPerspective.PredefinedCameraType.FIRST_PERSON
        CameraType.THIRD_PERSON_BACK -> CameraPerspective.PredefinedCameraType.THIRD_PERSON_BACK
        CameraType.THIRD_PERSON_FRONT -> CameraPerspective.PredefinedCameraType.THIRD_PERSON_FRONT
        else -> null // Some mod extend this enum...
    }

    override fun cycle(): CameraPerspective = from(cameraType.cycle())

    companion object : CameraPerspective.Factory {
        private val values = Array(CameraType.entries.size) { CameraPerspectiveImpl(CameraType.entries[it]) }

        fun from(cameraType: CameraType): CameraPerspective = values[cameraType.ordinal]

        @ActualConstructor
        @JvmStatic
        override fun firstPerson(): CameraPerspective = from(CameraType.FIRST_PERSON)

        @ActualConstructor
        @JvmStatic
        override fun thirdPersonBack(): CameraPerspective = from(CameraType.THIRD_PERSON_BACK)

        @ActualConstructor
        @JvmStatic
        override fun thirdPersonFront(): CameraPerspective = from(CameraType.THIRD_PERSON_FRONT)
    }
}
