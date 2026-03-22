package top.fifthlight.blazerod.model

import org.joml.Vector3fc
import java.util.*

data class RigidBody(
    val name: String? = null,
    val collisionGroup: Int,
    val collisionMask: Int,
    val shape: ShapeType,
    val shapeSize: Vector3fc,
    val shapePosition: Vector3fc,
    val shapeRotation: Vector3fc,
    val mass: Float,
    val moveAttenuation: Float,
    val rotationDamping: Float,
    val repulsion: Float,
    val frictionForce: Float,
    val physicsMode: PhysicsMode,
) {
    enum class ShapeType {
        SPHERE,
        BOX,
        CAPSULE,
    }

    enum class PhysicsMode {
        FOLLOW_BONE,
        PHYSICS,
        PHYSICS_PLUS_BONE,
    }
}

data class RigidBodyId(
    val modelId: UUID,
    val index: Int,
)
