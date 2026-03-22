package top.fifthlight.blazerod.model

sealed class Camera {
    abstract val name: String?

    data class MMD(
        override val name: String?,
    ) : Camera()

    data class Perspective(
        override val name: String?,
        val aspectRatio: Float? = null,
        val yfov: Float,
        val zfar: Float? = null,
        val znear: Float,
    ) : Camera()

    data class Orthographic(
        override val name: String?,
        val xmag: Float,
        val ymag: Float,
        val zfar: Float,
        val znear: Float,
    ) : Camera()
}
