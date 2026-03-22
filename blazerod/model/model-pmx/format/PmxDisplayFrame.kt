package top.fifthlight.blazerod.model.pmx.format

// Actually useless for us
data class PmxDisplayFrame(
    val nameLocal: String,
    val nameUniversal: String,
    val isSpecial: Boolean,
    val frames: List<FrameData>,
) {
    sealed class FrameData {
        data class Bone(val boneIndex: Int) : FrameData()
        data class Morph(val morphIndex: Int) : FrameData()
    }
}
