package top.fifthlight.blazerod.model.pmx.format

import org.joml.Vector3f

data class PmxJoint(
    val nameLocal: String,
    val nameUniversal: String,
    val type: JointType,
    val rigidBodyIndexA: Int,
    val rigidBodyIndexB: Int,
    val position: Vector3f,
    val rotation: Vector3f,
    val positionMinimum: Vector3f,
    val positionMaximum: Vector3f,
    val rotationMinimum: Vector3f,
    val rotationMaximum: Vector3f,
    val positionSpring: Vector3f,
    val rotationSpring: Vector3f,
) {
    enum class JointType(val value: Int) {
        SPRING_6DOF(0),
    }
}
