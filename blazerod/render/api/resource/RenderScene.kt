package top.fifthlight.blazerod.render.api.resource

import top.fifthlight.blazerod.render.api.refcount.RefCount
import top.fifthlight.blazerod.model.Camera
import top.fifthlight.blazerod.model.HumanoidTag
import top.fifthlight.blazerod.model.NodeId
import top.fifthlight.blazerod.model.NodeTransformView

interface RenderScene : RefCount {
    val rootNode: RenderNode
    val nodes: List<RenderNode>
    val expressions: List<RenderExpression>
    val expressionGroups: List<RenderExpressionGroup>
    val cameras: List<Camera>

    val ikTargetData: List<IkTargetData>
    val nodeIdMap: Map<NodeId, RenderNode>
    val nodeNameMap: Map<String, RenderNode>
    val humanoidTagMap: Map<HumanoidTag, RenderNode>

    val renderTransform: top.fifthlight.blazerod.model.NodeTransform?
    
    val attachments: Map<Class<*>, Any>

    data class IkTargetData(val effectorNode: RenderNode)
}
