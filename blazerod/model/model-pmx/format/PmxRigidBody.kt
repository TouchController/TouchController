package top.fifthlight.blazerod.model.pmx.format

import org.joml.Vector3f

data class PmxRigidBody(
    val nameLocal: String,
    val nameUniversal: String,
    val relatedBoneIndex: Int,
    val groupId: Int,
    val nonCollisionGroup: Int,
    val shape: ShapeType,
    val shapeSize: Vector3f,
    val shapePosition: Vector3f,
    val shapeRotation: Vector3f,
    val mass: Float,
    val moveAttenuation: Float,
    val rotationDamping: Float,
    val repulsion: Float,
    val frictionForce: Float,
    val physicsMode: PhysicsMode,
) {
    enum class ShapeType(val value: Int) {
        SPHERE(0),
        BOX(1),
        CAPSULE(2)
    }

    enum class PhysicsMode(val value: Int) {
        FOLLOW_BONE(0),
        PHYSICS(1),
        PHYSICS_PLUS_BONE(2)
    }
}
