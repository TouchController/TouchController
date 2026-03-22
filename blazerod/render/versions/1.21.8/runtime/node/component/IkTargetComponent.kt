package top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component

import net.minecraft.client.renderer.RenderType
import net.minecraft.util.CommonColors
import org.joml.*
import top.fifthlight.blazerod.model.TransformId
import top.fifthlight.blazerod.render.version_1_21_8.runtime.ModelInstanceImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.RenderNodeImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.UpdatePhase
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.getTransformMap
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.getWorldTransform
import kotlin.math.*

class IkTargetComponent(
    val ikIndex: Int,
    val limitRadian: Float,
    val loopCount: Int,
    val chains: List<Chain>,
    val effectorNodeIndex: Int,
    val transformId: TransformId,
) : RenderNodeComponent<IkTargetComponent>() {
    override fun onClosed() {}

    override val type: Type<IkTargetComponent>
        get() = Type.IkTarget

    companion object {
        private val updatePhases = listOf(
            UpdatePhase.Type.IK_UPDATE,
            UpdatePhase.Type.DEBUG_RENDER,
        )

        private const val FLOAT_PI = PI.toFloat()
        private const val FLOAT_TWO_PI = FLOAT_PI * 2

        private val decomposeTests = listOf(
            Vector3f(FLOAT_PI, FLOAT_PI, FLOAT_PI),   // + + +
            Vector3f(FLOAT_PI, FLOAT_PI, -FLOAT_PI),  // + + -
            Vector3f(FLOAT_PI, -FLOAT_PI, FLOAT_PI),  // + - +
            Vector3f(FLOAT_PI, -FLOAT_PI, -FLOAT_PI), // + - -
            Vector3f(-FLOAT_PI, FLOAT_PI, FLOAT_PI),  // - + +
            Vector3f(-FLOAT_PI, FLOAT_PI, -FLOAT_PI), // - + -
            Vector3f(-FLOAT_PI, -FLOAT_PI, FLOAT_PI), // - - +
            Vector3f(-FLOAT_PI, -FLOAT_PI, -FLOAT_PI) // - - -
        )
    }

    override val updatePhases: List<UpdatePhase.Type>
        get() = Companion.updatePhases

    class Chain(
        val nodeIndex: Int,
        val limit: top.fifthlight.blazerod.model.IkTarget.IkJoint.Limits?,
    ) {
        val prevAngle = Vector3f()
        val saveIKRot = Quaternionf()
        var planeModeAngle: Float = 0f
    }

    // I took the algorithm from https://github.com/benikabocha/saba/blob/master/src/Saba/Model/MMD/MMDIkSolver.cpp
    private fun normalizeAngle(angle: Float): Float {
        var ret = angle
        while (ret >= FLOAT_TWO_PI) {
            ret -= FLOAT_TWO_PI
        }
        while (ret < 0) {
            ret += FLOAT_TWO_PI
        }

        return ret
    }

    private fun diffAngle(a: Float, b: Float): Float = (normalizeAngle(a) - normalizeAngle(b)).let { diff ->
        when {
            diff > FLOAT_PI -> diff - FLOAT_TWO_PI
            diff < -FLOAT_PI -> diff + FLOAT_TWO_PI
            else -> diff
        }
    }

    private val testVec = Vector3f()
    private fun decompose(m: Matrix3fc, before: Vector3fc, r: Vector3f): Vector3f {
        val sy = -m.m02()
        val e = 1e-6f
        if ((1f - abs(sy)) < e) {
            r.y = asin(sy)
            // Find the closest angle to 180 degrees
            val sx = sin(before.x())
            val sz = sin(before.z())

            if (abs(sx) < abs(sz)) {
                // X is closer to 0 or 180
                val cx = cos(before.x())
                if (cx > 0) {
                    r.x = 0f
                    r.z = asin(-m.m10())
                } else {
                    r.x = FLOAT_PI
                    r.z = asin(m.m10())
                }
            } else {
                val cz = cos(before.z())
                if (cz > 0) {
                    r.z = 0f
                    r.x = asin(-m.m21())
                } else {
                    r.z = FLOAT_PI
                    r.x = asin(m.m21())
                }
            }
        } else {
            r.x = atan2(m.m12(), m.m22())
            r.y = asin(-m.m02())
            r.z = atan2(m.m01(), m.m00())
        }

        val errX = abs(diffAngle(r.x, before.x()))
        val errY = abs(diffAngle(r.y, before.y()))
        val errZ = abs(diffAngle(r.z, before.z()))
        var minErr = errX + errY + errZ
        for (testDiff in decomposeTests) {
            testVec.set(r.x, -r.y, r.z).add(testDiff)
            val err = abs(diffAngle(testVec.x(), before.x())) +
                    abs(diffAngle(testVec.y(), before.y())) +
                    abs(diffAngle(testVec.z(), before.z()))
            if (err < minErr) {
                minErr = err
                r.set(testVec)
            }
        }
        return r
    }

    private fun Vector3fc.getAxis(axis: top.fifthlight.blazerod.model.IkTarget.IkJoint.Limits.Axis) = when (axis) {
        top.fifthlight.blazerod.model.IkTarget.IkJoint.Limits.Axis.X -> x()
        top.fifthlight.blazerod.model.IkTarget.IkJoint.Limits.Axis.Y -> y()
        top.fifthlight.blazerod.model.IkTarget.IkJoint.Limits.Axis.Z -> z()
    }

    private fun Vector3f.coerceIn(min: Vector3fc, max: Vector3fc): Vector3f = set(
        x.coerceIn(min.x(), max.x()),
        y.coerceIn(min.y(), max.y()),
        z.coerceIn(min.z(), max.z()),
    )

    private fun Vector3f.coerceIn(min: Float, max: Float): Vector3f = set(
        x.coerceIn(min, max),
        y.coerceIn(min, max),
        z.coerceIn(min, max),
    )

    private val targetPos = Vector3f()
    private val ikPos = Vector3f()
    private val invChain = Matrix4f()
    private val chainIkPos = Vector3f()
    private val chainTargetPos = Vector3f()
    private val prevRotationInv = Quaternionf()

    private val cross = Vector3f()
    private val rot = Quaternionf()
    private val chainRot = Quaternionf()
    private val chainRotM = Matrix3f()
    private val rotXYZ = Vector3f()
    private fun solveCore(
        node: RenderNodeImpl,
        instance: ModelInstanceImpl,
        iterateCount: Int,
    ) {
        val ikPos = instance.getWorldTransform(effectorNodeIndex).getTranslation(ikPos)
        for (chain in chains) {
            if (chain.nodeIndex == node.nodeIndex) {
                // Avoid zero result, and NaN
                continue
            }
            val limit = chain.limit
            val axis = limit?.singleAxis
            if (axis != null) {
                solvePlane(node, instance, iterateCount, chain, chain.limit, axis)
                continue
            }

            val targetPos = instance.getWorldTransform(node).getTranslation(targetPos)
            val invChain = instance.getWorldTransform(chain.nodeIndex).invert(invChain)

            val chainIkPos = ikPos.mulPosition(invChain, chainIkPos)
            val chainTargetPos = targetPos.mulPosition(invChain, chainTargetPos)

            // Unnormalized vector seems never used then, so directly overwrite them
            val chainIkVec = chainIkPos.normalize()
            val chainTargetVec = chainTargetPos.normalize()

            val dot = chainTargetVec.dot(chainIkVec).coerceIn(-1f, 1f)

            var angle = acos(dot)
            // Why convert to degrees to compare? Just use radians is enough
            if (angle < 1e-5f) {
                continue
            }
            angle = angle.coerceIn(-limitRadian, limitRadian)
            val cross = chainTargetVec.cross(chainIkVec, cross).normalize()
            val rot = rot.rotationAxis(angle, cross)

            val chainRot = instance.getTransformMap(chain.nodeIndex)
                .getSum(transformId)
                .getUnnormalizedRotation(chainRot)
                .mul(rot)
            if (limit != null) {
                val chainRotM = chainRotM.rotation(chainRot)
                val rotXYZ = decompose(chainRotM, chain.prevAngle, rotXYZ)
                val clampXYZ = rotXYZ.coerceIn(limit.min, limit.max)
                    .sub(chain.prevAngle).coerceIn(-limitRadian, limitRadian).add(chain.prevAngle)
                // Don't introduce a temp r
                chainRotM.rotationXYZ(clampXYZ.x, clampXYZ.y, clampXYZ.z)
                chain.prevAngle.set(clampXYZ)

                chainRotM.getUnnormalizedRotation(chainRot)
            }

            val prevRotationInv = instance.getTransformMap(chain.nodeIndex)
                .getSum(transformId.prev)
                .getUnnormalizedRotation(prevRotationInv).invert()
            instance.setTransformDecomposed(chain.nodeIndex, transformId) {
                rotation.set(chainRot).mul(prevRotationInv)
            }
            instance.updateNodeTransform(chain.nodeIndex)
        }
    }

    private val rot1 = Quaternionf()
    private val targetVec1 = Vector3f()
    private val rot2 = Quaternionf()
    private val targetVec2 = Vector3f()
    private fun solvePlane(
        node: RenderNodeImpl,
        instance: ModelInstanceImpl,
        iterateCount: Int,
        chain: Chain,
        limits: top.fifthlight.blazerod.model.IkTarget.IkJoint.Limits,
        axis: top.fifthlight.blazerod.model.IkTarget.IkJoint.Limits.Axis,
    ) {
        val rotateAxis = axis.axis
        // Plane seems unused, so I removed it

        val ikPos = instance.getWorldTransform(effectorNodeIndex).getTranslation(ikPos)
        val targetPos = instance.getWorldTransform(node).getTranslation(targetPos)

        val invChain = instance.getWorldTransform(chain.nodeIndex).invert(invChain)

        val chainIkPos = ikPos.mulPosition(invChain, chainIkPos)
        val chainTargetPos = targetPos.mulPosition(invChain, chainTargetPos)

        // Unnormalized vector seems never used then, so directly overwrite them
        val chainIkVec = chainIkPos.normalize()
        val chainTargetVec = chainTargetPos.normalize()

        val dot = chainTargetVec.dot(chainIkVec).coerceIn(-1f, 1f)

        val angle = acos(dot).coerceIn(-limitRadian, limitRadian)
        // angleDeg is also unused

        val rot1 = rot1.rotationAxis(angle, rotateAxis)
        val targetVec1 = chainTargetVec.rotate(rot1, targetVec1)
        val dot1 = targetVec1.dot(chainIkVec)

        val rot2 = rot2.rotationAxis(-angle, rotateAxis)
        val targetVec2 = chainTargetVec.rotate(rot2, targetVec2)
        val dot2 = targetVec2.dot(chainIkVec)

        var newAngle = chain.planeModeAngle
        if (dot1 > dot2) {
            newAngle += angle
        } else {
            newAngle -= angle
        }

        val limitRange = limits.min.getAxis(axis)..limits.max.getAxis(axis)
        if (iterateCount == 0) {
            if (newAngle !in limitRange) {
                if (-newAngle in limitRange) {
                    newAngle = -newAngle
                } else {
                    val halfRad = (limitRange.start + limitRange.endInclusive) * .5f
                    if (abs(halfRad - newAngle) > abs(halfRad + newAngle)) {
                        newAngle = -newAngle
                    }
                }
            }
        }

        newAngle = newAngle.coerceIn(limitRange)
        chain.planeModeAngle = newAngle

        val prevRotationInv = instance.getTransformMap(chain.nodeIndex)
            .getSum(transformId.prev)
            .getUnnormalizedRotation(prevRotationInv).invert()
        instance.setTransformDecomposed(chain.nodeIndex, transformId) {
            rotation.rotationAxis(newAngle, rotateAxis).mul(prevRotationInv)
        }
        instance.updateNodeTransform(chain.nodeIndex)
    }

    override fun update(
        phase: UpdatePhase,
        node: RenderNodeImpl,
        instance: ModelInstanceImpl,
    ) {
        val enabled = instance.modelData.ikEnabled[ikIndex]
        if (!enabled) {
            return
        }
        when (phase) {
            is UpdatePhase.IkUpdate -> {
                if (chains.isEmpty()) {
                    return
                }
                for (chain in chains) {
                    chain.prevAngle.set(0f)
                    instance.setTransformDecomposed(chain.nodeIndex, transformId) {
                        rotation.identity()
                    }
                    chain.planeModeAngle = 0f
                }
                instance.updateNodeTransform(chains.last().nodeIndex)

                var maxDist = Float.MAX_VALUE
                for (i in 0 until loopCount) {
                    solveCore(node, instance, i)

                    val targetPos = instance.getWorldTransform(node).getTranslation(targetPos)
                    val ikPos = instance.getWorldTransform(effectorNodeIndex).getTranslation(ikPos)
                    // We use distanceSquared() here, unlike original code
                    val dist = targetPos.distanceSquared(ikPos)

                    if (dist < maxDist) {
                        maxDist = dist
                        for (chain in chains) {
                            val matrix = instance.getTransformMap(chain.nodeIndex).get(transformId)
                            if (matrix != null) {
                                matrix.getRotation(chain.saveIKRot)
                            } else {
                                chain.saveIKRot.identity()
                            }
                        }
                    } else {
                        for (chain in chains) {
                            instance.setTransformDecomposed(chain.nodeIndex, transformId) {
                                rotation.set(chain.saveIKRot)
                            }
                        }
                        instance.updateNodeTransform(chains.last().nodeIndex)
                        break
                    }
                }
            }

            is UpdatePhase.DebugRender -> {
                val consumers = phase.multiBufferSource

                val boxBuffer = consumers.getBuffer(RenderType.debugQuads())
                for (joint in chains) {
                    val jointMatrix =
                        phase.viewProjectionMatrix.mul(instance.getWorldTransform(joint.nodeIndex), Matrix4f())
                    boxBuffer.drawBox(jointMatrix, 0.005f, CommonColors.BLUE)
                }

                val effectorMatrix =
                    phase.viewProjectionMatrix.mul(instance.getWorldTransform(effectorNodeIndex), Matrix4f())
                boxBuffer.drawBox(effectorMatrix, 0.01f, CommonColors.RED)

                val targetMatrix = phase.viewProjectionMatrix.mul(instance.getWorldTransform(node), Matrix4f())
                boxBuffer.drawBox(targetMatrix, 0.01f, CommonColors.GREEN)

                val lineBuffer = consumers.getBuffer(DEBUG_RENDER_LAYER)
                for (joint in chains) {
                    val jointMatrix =
                        phase.viewProjectionMatrix.mul(instance.getWorldTransform(joint.nodeIndex), Matrix4f())
                    val lineSize = .05f
                    lineBuffer.addVertex(jointMatrix, 0.0f, 0.0f, 0.0f).setColor(CommonColors.RED)
                        .setNormal(0.0f, 1.0f, 0.0f)
                    lineBuffer.addVertex(jointMatrix, lineSize, 0.0f, 0.0f).setColor(CommonColors.RED)
                        .setNormal(0.0f, 1.0f, 0.0f)
                    lineBuffer.addVertex(jointMatrix, 0.0f, lineSize, 0.0f).setColor(CommonColors.GREEN)
                        .setNormal(0.0f, 1.0f, 0.0f)
                    lineBuffer.addVertex(jointMatrix, 0.0f, lineSize, lineSize).setColor(CommonColors.GREEN)
                        .setNormal(0.0f, 1.0f, 0.0f)
                    lineBuffer.addVertex(jointMatrix, lineSize, 0.0f, 0.0f).setColor(CommonColors.BLUE)
                        .setNormal(0.0f, 1.0f, 0.0f)
                    lineBuffer.addVertex(jointMatrix, lineSize, 0.0f, lineSize).setColor(CommonColors.BLUE)
                        .setNormal(0.0f, 1.0f, 0.0f)
                }
            }

            else -> {}
        }
    }
}
