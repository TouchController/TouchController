package top.fifthlight.blazerod.render.version_1_21_8.util.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import net.minecraft.util.thread.BlockableEventLoop
import kotlin.coroutines.CoroutineContext

class BlockableEventLoopDispatcher(private val executor: BlockableEventLoop<*>) : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext) = !executor.isSameThread

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (executor.isSameThread) {
            Dispatchers.Unconfined.dispatch(context, block)
        } else {
            executor.execute(block)
        }
    }
}
