package top.fifthlight.blazerod.render.version_1_21_8.runtime.test

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import top.fifthlight.blazerod.model.NodeTransform
import top.fifthlight.blazerod.model.TransformId
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.TransformMap
import kotlin.test.*

class TransformMapTest {

    private lateinit var transformMap: TransformMap

    @BeforeTest
    fun setUp() {
        transformMap = TransformMap(null)
    }

    @Test
    fun absoluteTransformIsInitializedCorrectly() {
        val absoluteTransform = transformMap.get(TransformId.ABSOLUTE)
        assertNotNull(absoluteTransform)
        assertTrue(absoluteTransform is NodeTransform.Decomposed)
        val decomposed = absoluteTransform as NodeTransform.Decomposed
        assertEquals(Vector3f(0f, 0f, 0f), decomposed.translation)
        assertEquals(Vector3f(1f, 1f, 1f), decomposed.scale)
        assertEquals(Quaternionf(), decomposed.rotation)

        val absoluteSumMatrix = transformMap.getSum(TransformId.ABSOLUTE)
        assertTrue(absoluteSumMatrix.equals(Matrix4f(), 1e-6f))
    }

    @Test
    fun getReturnsNullForNonExistentTransformIds() {
        assertNull(transformMap.get(TransformId.RELATIVE_ANIMATION))
        assertNull(transformMap.get(TransformId.INFLUENCE))
    }

    @Test
    fun updateDecomposedCorrectlyAddsNewDecomposedTransform() {
        val translation = Vector3f(1f, 2f, 3f)
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) {
            this.translation.set(translation)
        }

        val transform = transformMap.get(TransformId.RELATIVE_ANIMATION)
        assertNotNull(transform)
        assertTrue(transform is NodeTransform.Decomposed)
        assertEquals(translation, (transform as NodeTransform.Decomposed).translation)

        val sumMatrix = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        val expectedMatrix = Matrix4f().translate(translation)
        assertTrue(expectedMatrix.equals(sumMatrix, 1e-6f))
    }

    @Test
    fun updateDecomposedCorrectlyUpdatesExistingDecomposedTransform() {
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) {
            translation.set(1f, 2f, 3f)
        }

        val newTranslation = Vector3f(4f, 5f, 6f)
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) {
            translation.set(newTranslation)
        }

        val transform = transformMap.get(TransformId.RELATIVE_ANIMATION)
        assertNotNull(transform)
        assertTrue(transform is NodeTransform.Decomposed)
        assertEquals(newTranslation, (transform as NodeTransform.Decomposed).translation)

        val sumMatrix = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        val expectedMatrix = Matrix4f().translate(newTranslation)
        assertTrue(expectedMatrix.equals(sumMatrix, 1e-6f))
    }

    @Test
    fun updateMatrixCorrectlyAddsNewMatrixTransform() {
        val testMatrix = Matrix4f().translate(10f, 20f, 30f).rotateX(Math.toRadians(90.0).toFloat())
        transformMap.updateMatrix(TransformId.INFLUENCE) {
            matrix.set(testMatrix)
        }

        val transform = transformMap.get(TransformId.INFLUENCE)
        assertNotNull(transform)
        assertTrue(transform is NodeTransform.Matrix)
        assertTrue(testMatrix.equals((transform as NodeTransform.Matrix).matrix, 1e-6f))

        val sumMatrix = transformMap.getSum(TransformId.INFLUENCE)
        val expectedMatrix = Matrix4f(transformMap.getSum(TransformId.RELATIVE_ANIMATION)).mul(testMatrix)
        assertTrue(expectedMatrix.equals(sumMatrix, 1e-6f))
    }

    @Test
    fun updateMatrixCorrectlyUpdatesExistingMatrixTransform() {
        val initialMatrix = Matrix4f().translate(1f, 0f, 0f)
        transformMap.updateMatrix(TransformId.INFLUENCE) {
            matrix.set(initialMatrix)
        }

        val newMatrix = Matrix4f().translate(0f, 1f, 0f)
        transformMap.updateMatrix(TransformId.INFLUENCE) {
            matrix.set(newMatrix)
        }

        val transform = transformMap.get(TransformId.INFLUENCE)
        assertNotNull(transform)
        assertTrue(transform is NodeTransform.Matrix)
        assertTrue(newMatrix.equals((transform as NodeTransform.Matrix).matrix, 1e-6f))

        val sumMatrix = transformMap.getSum(TransformId.INFLUENCE)
        val expectedMatrix = Matrix4f(transformMap.getSum(TransformId.RELATIVE_ANIMATION)).mul(newMatrix)
        assertTrue(expectedMatrix.equals(sumMatrix, 1e-6f))
    }

    @Test
    fun updateDecomposedConvertsMatrixToDecomposedWhenTypeMismatch() {
        val initialMatrix = Matrix4f().translate(5f, 0f, 0f).scale(2f)
        transformMap.updateMatrix(TransformId.IK) {
            matrix.set(initialMatrix)
        }

        val newTranslation = Vector3f(10f, 10f, 10f)
        transformMap.updateDecomposed(TransformId.IK) {
            translation.set(newTranslation)
        }

        val transform = transformMap.get(TransformId.IK)
        assertNotNull(transform)
        val decomposedTransform = transform!!.toDecomposed()
        assertEquals(newTranslation, decomposedTransform.translation)
        assertEquals(Vector3f(2f, 2f, 2f), decomposedTransform.scale)
        assertTrue(decomposedTransform.rotation.equals(Quaternionf(), 1e-6f))

        val sumMatrix = transformMap.getSum(TransformId.IK)
        val expectedMatrix = Matrix4f(transformMap.getSum(TransformId.INFLUENCE))
            .translate(newTranslation)
            .scale(Vector3f(2f, 2f, 2f))
        assertTrue(expectedMatrix.equals(sumMatrix, 1e-6f))
    }

    @Test
    fun updateMatrixConvertsDecomposedToMatrixWhenTypeMismatch() {
        val initialTranslation = Vector3f(1f, 1f, 1f)
        val initialRotation = Quaternionf().rotateX(Math.toRadians(90.0).toFloat())
        transformMap.updateDecomposed(TransformId.IK) {
            translation.set(initialTranslation)
            rotation.set(initialRotation)
        }

        val newMatrix = Matrix4f().translate(0f, 0f, 5f)
        transformMap.updateMatrix(TransformId.IK) {
            matrix.set(newMatrix)
        }

        val transform = transformMap.get(TransformId.IK)
        assertNotNull(transform)
        assertTrue(transform is NodeTransform.Matrix)
        val matrixTransform = transform as NodeTransform.Matrix
        assertTrue(newMatrix.equals(matrixTransform.matrix, 1e-6f))

        val sumMatrix = transformMap.getSum(TransformId.IK)
        val expectedMatrix = Matrix4f(transformMap.getSum(TransformId.INFLUENCE)).mul(newMatrix)
        assertTrue(expectedMatrix.equals(sumMatrix, 1e-6f))
    }

    @Test
    fun getSumCalculatesCumulativeTransformCorrectlyAcrossMultipleTypes() {
        val relAnimTranslation = Vector3f(10f, 0f, 0f)
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) {
            translation.set(relAnimTranslation)
        }

        val influenceMatrix = Matrix4f().rotateY(Math.toRadians(90.0).toFloat())
        transformMap.updateMatrix(TransformId.INFLUENCE) {
            matrix.set(influenceMatrix)
        }

        val ikScale = Vector3f(2f, 2f, 2f)
        transformMap.updateDecomposed(TransformId.IK) {
            scale.set(ikScale)
        }

        val physicsMatrix = Matrix4f().translate(0f, 5f, 0f)
        transformMap.updateMatrix(TransformId.PHYSICS) {
            matrix.set(physicsMatrix)
        }

        val expectedSumMatrix =
            Matrix4f().translate(relAnimTranslation).mul(influenceMatrix).scale(ikScale).mul(physicsMatrix)
        val actualSumMatrix = transformMap.getSum(TransformId.PHYSICS)

        assertTrue(expectedSumMatrix.equals(actualSumMatrix, 1e-5f))
    }

    @Test
    fun modifyingPrecedingTransformMakesSubsequentSumsDirtyAndRecalculated() {
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) { translation.set(1f, 0f, 0f) }
        transformMap.updateMatrix(TransformId.INFLUENCE) { matrix.translate(0f, 1f, 0f) }
        transformMap.updateDecomposed(TransformId.IK) { translation.set(0f, 0f, 1f) }

        val initialIKSum = Matrix4f(transformMap.getSum(TransformId.IK))
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) { translation.set(10f, 0f, 0f) }
        val newIKSum = transformMap.getSum(TransformId.IK)
        assertFalse(initialIKSum.equals(newIKSum, 1e-6f))

        val expectedNewIKSum = Matrix4f()
            .translate(10f, 0f, 0f)
            .translate(0f, 1f, 0f)
            .translate(0f, 0f, 1f)
        assertTrue(expectedNewIKSum.equals(newIKSum, 1e-6f))
    }

    @Test
    fun sumOfNonExistentIntermediateTransformShouldBeIdentity() {
        val influenceMatrix = Matrix4f().translate(5f, 0f, 0f)
        transformMap.updateMatrix(TransformId.INFLUENCE) { matrix.set(influenceMatrix) }

        val expectedSum = Matrix4f().translate(5f, 0f, 0f)
        val actualSum = transformMap.getSum(TransformId.INFLUENCE)

        assertTrue(expectedSum.equals(actualSum, 1e-6f))
    }

    @Test
    fun canUpdateAbsoluteTransform() {
        val newAbsoluteTranslation = Vector3f(100f, 100f, 100f)
        transformMap.updateDecomposed(TransformId.ABSOLUTE) {
            translation.set(newAbsoluteTranslation)
        }

        val absoluteTransform = transformMap.get(TransformId.ABSOLUTE)
        assertNotNull(absoluteTransform)
        assertTrue(absoluteTransform is NodeTransform.Decomposed)
        assertEquals(newAbsoluteTranslation, (absoluteTransform as NodeTransform.Decomposed).translation)

        val absoluteSumMatrix = transformMap.getSum(TransformId.ABSOLUTE)
        val expectedMatrix = Matrix4f().translate(newAbsoluteTranslation)
        assertTrue(expectedMatrix.equals(absoluteSumMatrix, 1e-6f))
    }


    @Test
    fun updateAbsoluteWithMatrixType() {
        val newAbsoluteMatrix = Matrix4f().rotateZ(Math.toRadians(45.0).toFloat()).translate(1f, 2f, 3f)
        transformMap.updateMatrix(TransformId.ABSOLUTE) {
            matrix.set(newAbsoluteMatrix)
        }

        val absoluteTransform = transformMap.get(TransformId.ABSOLUTE)
        assertNotNull(absoluteTransform)
        assertTrue(absoluteTransform is NodeTransform.Matrix)
        assertTrue(newAbsoluteMatrix.equals((absoluteTransform as NodeTransform.Matrix).matrix, 1e-6f))

        val absoluteSumMatrix = transformMap.getSum(TransformId.ABSOLUTE)
        assertTrue(newAbsoluteMatrix.equals(absoluteSumMatrix, 1e-6f))
    }

    @Test
    fun updateAbsoluteMatrixToDecomposed() {
        val initialMatrix = Matrix4f().translate(10f, 20f, 30f).scale(5f)
        transformMap.updateMatrix(TransformId.ABSOLUTE) {
            matrix.set(initialMatrix)
        }

        val newTranslation = Vector3f(1f, 1f, 1f)
        transformMap.updateDecomposed(TransformId.ABSOLUTE) {
            translation.set(newTranslation)
        }

        val absoluteTransform = transformMap.get(TransformId.ABSOLUTE)
        assertNotNull(absoluteTransform)
        assertTrue(absoluteTransform is NodeTransform.Decomposed)
        val decomposed = absoluteTransform as NodeTransform.Decomposed

        assertEquals(newTranslation, decomposed.translation)
        assertEquals(Vector3f(5f, 5f, 5f), decomposed.scale)
        assertTrue(decomposed.rotation.equals(Quaternionf(), 1e-6f))

        val absoluteSumMatrix = transformMap.getSum(TransformId.ABSOLUTE)
        val expectedMatrix = Matrix4f().translate(newTranslation).scale(5f)
        assertTrue(expectedMatrix.equals(absoluteSumMatrix, 1e-6f))
    }

    @Test
    fun getSumWithSparseTransforms() {
        val physicsTranslation = Vector3f(0f, 0f, 100f)
        transformMap.updateDecomposed(TransformId.PHYSICS) {
            translation.set(physicsTranslation)
        }

        val physicsSum = transformMap.getSum(TransformId.PHYSICS)

        val expectedSum = Matrix4f().translate(physicsTranslation)
        assertTrue(expectedSum.equals(physicsSum, 1e-6f))
    }

    @Test
    fun consecutiveUpdatesOfSameTransform() {
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) {
            translation.set(1f, 0f, 0f)
        }
        val firstSum = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        val expectedFirstSum = Matrix4f().translate(1f, 0f, 0f)
        assertTrue(expectedFirstSum.equals(firstSum, 1e-6f))

        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) {
            translation.set(2f, 0f, 0f)
        }
        val secondSum = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        val expectedSecondSum = Matrix4f().translate(2f, 0f, 0f)
        assertTrue(expectedSecondSum.equals(secondSum, 1e-6f))

        val newMatrix = Matrix4f().rotateY(Math.toRadians(90.0).toFloat())
        transformMap.updateMatrix(TransformId.RELATIVE_ANIMATION) {
            matrix.set(newMatrix)
        }
        val thirdSum = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        assertTrue(newMatrix.equals(thirdSum, 1e-6f))
    }

    @Test
    fun getSumAfterUpdatingNonAbsoluteTransform() {
        val initialSum = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        assertTrue(initialSum.equals(Matrix4f(), 1e-6f))

        val influenceTranslation = Vector3f(0f, 5f, 0f)
        transformMap.updateDecomposed(TransformId.INFLUENCE) {
            translation.set(influenceTranslation)
        }

        val actualSum = transformMap.getSum(TransformId.INFLUENCE)
        val expectedSum = Matrix4f().translate(influenceTranslation)
        assertTrue(expectedSum.equals(actualSum, 1e-6f))
    }

    @Test
    fun gettingSumOfPrecedingTransformAfterUpdate() {
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) {
            translation.set(1f, 0f, 0f)
        }
        val relAnimSumBeforeIKUpdate = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        val expectedRelAnimSum = Matrix4f().translate(1f, 0f, 0f)
        assertTrue(expectedRelAnimSum.equals(relAnimSumBeforeIKUpdate, 1e-6f))

        transformMap.updateDecomposed(TransformId.IK) {
            translation.set(0f, 0f, 10f)
        }

        val relAnimSumAfterIKUpdate = transformMap.getSum(TransformId.RELATIVE_ANIMATION)
        assertTrue(expectedRelAnimSum.equals(relAnimSumAfterIKUpdate, 1e-6f))
    }

    @Test
    fun repeatedGetSumCallsWithoutUpdatesUsesCache() {
        transformMap.updateDecomposed(TransformId.IK) {
            translation.set(1f, 2f, 3f)
        }

        val firstSum = transformMap.getSum(TransformId.IK)
        val secondSum = transformMap.getSum(TransformId.IK)
        val thirdSum = transformMap.getSum(TransformId.IK)

        assertTrue(firstSum.equals(secondSum, 1e-6f))
        assertTrue(firstSum.equals(thirdSum, 1e-6f))
    }

    @Test
    fun clearFromRemovesCorrectTransforms() {
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) { translation.set(1f, 0f, 0f) }
        transformMap.updateMatrix(TransformId.INFLUENCE) { matrix.translate(0f, 1f, 0f) }
        transformMap.updateDecomposed(TransformId.IK) { translation.set(0f, 0f, 1f) }
        transformMap.updateMatrix(TransformId.PHYSICS) { matrix.translate(0f, 0f, 0f) } // Add one more transform

        assertNotNull(transformMap.get(TransformId.RELATIVE_ANIMATION))
        assertNotNull(transformMap.get(TransformId.INFLUENCE))
        assertNotNull(transformMap.get(TransformId.IK))
        assertNotNull(transformMap.get(TransformId.PHYSICS))

        val initialPhysicsSum = transformMap.getSum(TransformId.PHYSICS)
        assertTrue(initialPhysicsSum.equals(Matrix4f().translate(1f, 1f, 1f), 1e-6f))

        transformMap.clearFrom(TransformId.IK)

        assertNotNull(transformMap.get(TransformId.RELATIVE_ANIMATION))
        assertNotNull(transformMap.get(TransformId.INFLUENCE))
        assertNull(transformMap.get(TransformId.IK))
        assertNull(transformMap.get(TransformId.PHYSICS))

        assertNotNull(transformMap.getSum(TransformId.RELATIVE_ANIMATION))
        assertNotNull(transformMap.getSum(TransformId.INFLUENCE))

        val expectedIKSumAfterClear = transformMap.getSum(TransformId.INFLUENCE)
        assertTrue(expectedIKSumAfterClear.equals(transformMap.getSum(TransformId.IK), 1e-6f))
        assertTrue(expectedIKSumAfterClear.equals(transformMap.getSum(TransformId.PHYSICS), 1e-6f))
    }

    @Test
    fun clearFromDefaultIdClearsAllButAbsolute() {
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) { translation.set(1f, 0f, 0f) }
        transformMap.updateMatrix(TransformId.INFLUENCE) { matrix.translate(0f, 1f, 0f) }
        transformMap.updateDecomposed(TransformId.IK) { translation.set(0f, 0f, 1f) }

        assertNotNull(transformMap.get(TransformId.ABSOLUTE))
        assertNotNull(transformMap.get(TransformId.RELATIVE_ANIMATION))
        assertNotNull(transformMap.get(TransformId.INFLUENCE))
        assertNotNull(transformMap.get(TransformId.IK))

        transformMap.clearFrom(TransformId.RELATIVE_ANIMATION)

        assertNotNull(transformMap.get(TransformId.ABSOLUTE))
        assertNull(transformMap.get(TransformId.RELATIVE_ANIMATION))
        assertNull(transformMap.get(TransformId.INFLUENCE))
        assertNull(transformMap.get(TransformId.IK))

        assertNotNull(transformMap.getSum(TransformId.ABSOLUTE))

        val expectedRelAnimSumAfterClear = transformMap.getSum(TransformId.ABSOLUTE)
        assertTrue(expectedRelAnimSumAfterClear.equals(transformMap.getSum(TransformId.RELATIVE_ANIMATION), 1e-6f))
        assertTrue(expectedRelAnimSumAfterClear.equals(transformMap.getSum(TransformId.INFLUENCE), 1e-6f))
        assertTrue(expectedRelAnimSumAfterClear.equals(transformMap.getSum(TransformId.IK), 1e-6f))
    }

    @Test
    fun clearFromHandlesEmptyMap() {
        transformMap.clearFrom(TransformId.RELATIVE_ANIMATION)

        assertNotNull(transformMap.get(TransformId.ABSOLUTE))
        assertNull(transformMap.get(TransformId.RELATIVE_ANIMATION))
        assertNull(transformMap.get(TransformId.INFLUENCE))
    }

    @Test
    fun clearFromDoesNotAffectPrecedingTransforms() {
        transformMap.updateDecomposed(TransformId.RELATIVE_ANIMATION) { translation.set(1f, 0f, 0f) }
        transformMap.updateMatrix(TransformId.INFLUENCE) { matrix.translate(0f, 1f, 0f) }
        transformMap.updateDecomposed(TransformId.IK) { translation.set(0f, 0f, 1f) }

        val initialRelAnimTransform = transformMap.get(TransformId.RELATIVE_ANIMATION)
        val initialRelAnimSum = transformMap.getSum(TransformId.RELATIVE_ANIMATION)

        transformMap.clearFrom(TransformId.IK)

        assertNotNull(transformMap.get(TransformId.RELATIVE_ANIMATION))
        assertNotNull(transformMap.get(TransformId.INFLUENCE))
        assertNull(transformMap.get(TransformId.IK))

        assertTrue(
            initialRelAnimTransform!!.matrix.equals(
                transformMap.get(TransformId.RELATIVE_ANIMATION)!!.matrix,
                1e-6f
            )
        )
        assertTrue(initialRelAnimSum.equals(transformMap.getSum(TransformId.RELATIVE_ANIMATION), 1e-6f))

        val expectedInfluenceSum =
            Matrix4f().translate(1f, 0f, 0f).translate(0f, 1f, 0f) // Sum of REL_ANIM and INFLUENCE
        assertTrue(expectedInfluenceSum.equals(transformMap.getSum(TransformId.INFLUENCE), 1e-6f))
    }
}
