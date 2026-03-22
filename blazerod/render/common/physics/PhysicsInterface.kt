package top.fifthlight.blazerod.physics

object PhysicsInterface {
    val isPhysicsAvailable
        get() = PhysicsLibrary.isPhysicsAvailable()

    fun load() = PhysicsLibrary.load()
}
