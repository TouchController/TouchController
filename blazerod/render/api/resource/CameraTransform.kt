package top.fifthlight.blazerod.render.api.resource

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc

interface CameraTransform {
    fun getMatrix(matrix: Matrix4f, aspectRatio: Float, farPlaneDistance: Float)

    val rotationQuaternion: Quaternionf
    val rotationEulerAngles: Vector3fc
    val position: Vector3fc

    interface MMD : CameraTransform {
        val targetPosition: Vector3f
        val fov: Float
        val distance: Float
    }

    interface Perspective : CameraTransform {
        val yfov: Float
        val zfar: Float?
        val znear: Float
    }

    interface Orthographic : CameraTransform {
        val xmag: Float
        val ymag: Float
        val zfar: Float
        val znear: Float
    }
}
