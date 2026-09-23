package top.fifthlight.pathflow.resolvers.noop.test

import top.fifthlight.pathflow.resolvers.noop.NoopResolver
import kotlin.test.Test
import kotlin.test.assertNull

class NoopResolverTest {
    @Test
    fun testEmpty() {
        assertNull(
            NoopResolver.resolve(
                inputFeatures = emptySet(),
                targetFeatures = emptySet(),
                transformers = emptySet(),
                maxSteps = 0,
            )
        )
    }
}
