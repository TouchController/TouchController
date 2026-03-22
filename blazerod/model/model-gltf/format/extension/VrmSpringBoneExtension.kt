package top.fifthlight.blazerod.model.gltf.format.extension

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector3f
import top.fifthlight.blazerod.model.gltf.format.Vector3fSerializer

@Serializable
data class VrmSpringBoneExtension(
    val specVersion: String,
    val colliders: List<Collider>? = null,
    val colliderGroups: List<ColliderGroup>? = null,
    val springs: List<Spring>? = null,
) {
    @Serializable
    data class Collider(
        val node: Int,
        val shape: Shape,
    ) {
        @Serializable
        sealed class Shape {
            @Serializable
            @SerialName("sphere")
            data class Sphere(
                @Serializable(with = Vector3fSerializer::class)
                val offset: Vector3f = Vector3f(),
                val radius: Float = 0f,
            ) : Shape()

            @Serializable
            @SerialName("capsule")
            data class Capsule(
                @Serializable(with = Vector3fSerializer::class)
                val offset: Vector3f = Vector3f(),
                val radius: Float = 0f,
                @Serializable(with = Vector3fSerializer::class)
                val tail: Vector3f = Vector3f(),
            ) : Shape()
        }
    }

    @Serializable
    data class ColliderGroup(
        val name: String? = null,
        val colliders: List<Int> = listOf(),
    )

    @Serializable
    data class Spring(
        val name: String? = null,
        val joints: List<Joint>,
        val colliderGroups: List<Int> = listOf(),
        val center: Int? = null,
    ) {
        @Serializable
        data class Joint(
            val node: Int,
            val hitRadius: Float = 0.0f,
            val stiffness: Float = 1.0f,
            val gravityPower: Float = 0.0f,
            @Serializable(with = Vector3fSerializer::class)
            val gravityDir: Vector3f = Vector3f(0f, -1f, 0f),
            val dragForce: Float = 0.5f,
        )
    }
}
