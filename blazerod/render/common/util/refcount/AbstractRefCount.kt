package top.fifthlight.blazerod.render.common.util.refcount

import top.fifthlight.blazerod.render.api.refcount.InvalidReferenceCountException
import top.fifthlight.blazerod.render.api.refcount.RefCount
import top.fifthlight.blazerod.render.common.debug.ResourceCountTracker
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractRefCount : RefCount {
    final override var closed: Boolean = false
        private set

    private val initialized = AtomicBoolean(false)

    private val referenceCountAtomic = AtomicInteger(0)
    final override val referenceCount
        get() = referenceCountAtomic.get()

    abstract val typeId: String

    protected fun requireNotClosed() = require(!closed) { "Object $this is already closed." }

    override fun increaseReferenceCount() {
        requireNotClosed()
        if (referenceCountAtomic.getAndIncrement() < 0) {
            throw InvalidReferenceCountException(this, referenceCount)
        }
        if (initialized.compareAndSet(false, true)) {
            ResourceCountTracker.instance?.increase(typeId)
        }
    }

    override fun decreaseReferenceCount() {
        requireNotClosed()
        check(initialized.get()) { "Object $this is not initialized." }
        val refCount = referenceCountAtomic.decrementAndGet()
        when {
            refCount < 0 -> throw InvalidReferenceCountException(this, referenceCount)
            refCount == 0 -> {
                closed = true
                ResourceCountTracker.instance?.decrease(typeId)
                onClosed()
            }
        }
    }

    /**
     * Reset the state.
     *
     * It is designed to be used for objects being pooled. When the object is returned to the pool,
     * you can call this method to reset closed state, so it can be reused.
     */
    protected fun resetState() {
        require(closed) { "Object $this is not closed when resetting reference count." }
        require(referenceCount == 0) { "Object $this has reference count $referenceCount when resetting reference count." }
        initialized.set(false)
        closed = false
    }

    protected abstract fun onClosed()
}
