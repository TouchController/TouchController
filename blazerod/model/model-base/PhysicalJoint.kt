
package top.fifthlight.blazerod.model

import org.joml.Vector3fc

data class PhysicalJoint(
    val name: String? = null,
    val type: JointType,
    val rigidBodyA: RigidBodyId,
    val rigidBodyB: RigidBodyId,
    val position: Vector3fc,
    val rotation: Vector3fc,
    val positionMin: Vector3fc,
    val positionMax: Vector3fc,
    val rotationMin: Vector3fc,
    val rotationMax: Vector3fc,
    val positionSpring: Vector3fc,
    val rotationSpring: Vector3fc,
) {
    enum class JointType {
        SPRING_6DOF,
    }
}
