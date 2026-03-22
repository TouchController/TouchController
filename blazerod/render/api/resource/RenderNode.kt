package top.fifthlight.blazerod.render.api.resource

import top.fifthlight.blazerod.render.api.refcount.RefCount
import top.fifthlight.blazerod.model.HumanoidTag
import top.fifthlight.blazerod.model.NodeId
import top.fifthlight.blazerod.model.NodeTransformView

interface RenderNode : RefCount {
    val nodeIndex: Int
    val absoluteTransform: NodeTransformView?

    val nodeId: NodeId?
    val nodeName: String?
    val humanoidTags: List<HumanoidTag>
}
