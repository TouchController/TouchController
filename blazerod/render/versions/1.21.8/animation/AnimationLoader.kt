package top.fifthlight.blazerod.render.version_1_21_8.animation

import org.joml.Quaternionf
import org.joml.Vector3f
import top.fifthlight.blazerod.render.version_1_21_8.animation.AnimationChannelItem.*
import top.fifthlight.blazerod.model.animation.Animation
import top.fifthlight.blazerod.model.animation.AnimationChannel
import top.fifthlight.blazerod.model.util.MutableBoolean
import top.fifthlight.blazerod.model.util.MutableFloat
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component.RenderNodeComponent
import top.fifthlight.blazerod.render.version_1_21_8.runtime.RenderSceneImpl

object AnimationLoader {
    fun load(
        scene: RenderSceneImpl,
        animation: Animation,
    ): AnimationItemImpl {
        fun AnimationChannel.Type.NodeData.findTargetTransformIndex(): Int? {
            val node = targetNode?.id.let { nodeId -> scene.nodeIdMap[nodeId] }
                ?: targetNodeName?.let { name -> scene.nodeNameMap[name] }
                ?: targetHumanoidTag?.let { humanoid ->
                    scene.humanoidTagMap[humanoid]
                }
            return node?.nodeIndex
        }

        @Suppress("UNCHECKED_CAST")
        fun mapAnimationChannel(channel: AnimationChannel<*, *>): AnimationChannelItem<*, *, *>? {
            return when (channel.type) {
                AnimationChannel.Type.Translation -> {
                    val data = channel.typeData as AnimationChannel.Type.TransformData
                    TranslationItem(
                        index = data.node.findTargetTransformIndex() ?: return null,
                        transformId = data.transformId,
                        channel = channel as AnimationChannel<Vector3f, Unit>,
                    )
                }

                AnimationChannel.Type.Scale -> {
                    val data = channel.typeData as AnimationChannel.Type.TransformData
                    ScaleItem(
                        index = data.node.findTargetTransformIndex() ?: return null,
                        transformId = data.transformId,
                        channel = channel as AnimationChannel<Vector3f, Unit>,
                    )
                }

                AnimationChannel.Type.Rotation -> {
                    val data = channel.typeData as AnimationChannel.Type.TransformData
                    RotationItem(
                        index = data.node.findTargetTransformIndex() ?: return null,
                        transformId = data.transformId,
                        channel = channel as AnimationChannel<Quaternionf, Unit>,
                    )
                }

                AnimationChannel.Type.BedrockTranslation -> {
                    val data = channel.typeData as AnimationChannel.Type.TransformData
                    BedrockTranslationItem(
                        index = data.node.findTargetTransformIndex() ?: return null,
                        transformId = data.transformId,
                        channel = channel as AnimationChannel<Vector3f, Unit>,
                    )
                }

                AnimationChannel.Type.BedrockScale -> {
                    val data = channel.typeData as AnimationChannel.Type.TransformData
                    BedrockScaleItem(
                        index = data.node.findTargetTransformIndex() ?: return null,
                        transformId = data.transformId,
                        channel = channel as AnimationChannel<Vector3f, Unit>,
                    )
                }

                AnimationChannel.Type.BedrockRotation -> {
                    val data = channel.typeData as AnimationChannel.Type.TransformData
                    BedrockRotationItem(
                        index = data.node.findTargetTransformIndex() ?: return null,
                        transformId = data.transformId,
                        channel = channel as AnimationChannel<Quaternionf, Unit>,
                    )
                }

                AnimationChannel.Type.Morph -> {
                    val data = channel.typeData as AnimationChannel.Type.MorphData
                    MorphItem(
                        primitiveIndex = data.nodeData.findTargetTransformIndex() ?: return null,
                        targetGroupIndex = data.targetMorphGroupIndex,
                        channel = channel as AnimationChannel<MutableFloat, AnimationChannel.Type.MorphData>,
                    )
                }

                AnimationChannel.Type.Expression -> {
                    val channel =
                        channel as AnimationChannel<MutableFloat, AnimationChannel.Type.ExpressionData>
                    val data = channel.typeData
                    scene.expressions.firstOrNull { (it.name != null && it.name == data.name) || (it.tag != null && it.tag == data.tag) }
                        ?.let { ExpressionItem(it, channel) }
                        ?: scene.expressionGroups.firstOrNull { (it.name != null && it.name == data.name) || (it.tag != null && it.tag == data.tag) }
                            ?.let { ExpressionGroupItem(it, channel) }
                }

                AnimationChannel.Type.CameraFov -> {
                    val data = channel.typeData as AnimationChannel.Type.CameraData
                    val cameraIndex =
                        scene.cameras.indexOfFirst { it.name == data.cameraName }.takeIf { it >= 0 }
                            ?: return null
                    CameraFovItem(
                        cameraIndex = cameraIndex,
                        channel = channel as AnimationChannel<MutableFloat, AnimationChannel.Type.CameraData>,
                    )
                }

                AnimationChannel.Type.MMDCameraDistance -> {
                    val data = channel.typeData as AnimationChannel.Type.CameraData
                    val cameraIndex =
                        scene.cameras.indexOfFirst { it.name == data.cameraName }.takeIf { it >= 0 }
                            ?: return null
                    MMDCameraDistanceItem(
                        cameraIndex = cameraIndex,
                        channel = channel as AnimationChannel<MutableFloat, AnimationChannel.Type.CameraData>,
                    )
                }

                AnimationChannel.Type.MMDCameraRotation -> {
                    val data = channel.typeData as AnimationChannel.Type.CameraData
                    val cameraIndex =
                        scene.cameras.indexOfFirst { it.name == data.cameraName }.takeIf { it >= 0 }
                            ?: return null
                    MMDCameraRotationItem(
                        cameraIndex = cameraIndex,
                        channel = channel as AnimationChannel<Vector3f, AnimationChannel.Type.CameraData>,
                    )
                }

                AnimationChannel.Type.MMDCameraTarget -> {
                    val data = channel.typeData as AnimationChannel.Type.CameraData
                    val cameraIndex =
                        scene.cameras.indexOfFirst { it.name == data.cameraName }.takeIf { it >= 0 }
                            ?: return null
                    MMDCameraTargetItem(
                        cameraIndex = cameraIndex,
                        channel = channel as AnimationChannel<Vector3f, AnimationChannel.Type.CameraData>,
                    )
                }

                AnimationChannel.Type.IkEnabled -> {
                    val data = channel.typeData as AnimationChannel.Type.NodeData
                    val node = data.targetNode?.let { scene.nodeIdMap[it.id] }
                        ?: data.targetNodeName?.let { scene.nodeNameMap[it] }
                        ?: data.targetHumanoidTag?.let { scene.humanoidTagMap[it] }
                        ?: return null

                    val ikComponent = node.getComponentsOfType(RenderNodeComponent.Type.IkTarget).firstOrNull() ?: return null

                    @Suppress("UNCHECKED_CAST")
                    IkEnabledItem(
                        ikIndex = ikComponent.ikIndex,
                        channel = channel as AnimationChannel<MutableBoolean, AnimationChannel.Type.NodeData>,
                    )
                }
            }
        }

        return AnimationItemImpl(
            name = animation.name,
            animation = animation,
            channels = animation.channels.mapNotNull(::mapAnimationChannel)
        )
    }
}
