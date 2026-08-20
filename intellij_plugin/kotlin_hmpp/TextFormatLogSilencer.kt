package top.fifthlight.intellij.kotlinhmpp

import com.google.protobuf.TextFormat
import java.util.logging.Filter
import java.util.logging.Logger

/**
 * Avoid Protobuf's [com.google.protobuf.TextFormat] floods logs when HMPP info is
 * being parsed.
 */
internal object TextFormatLogSilencer {
    init {
        val logger = Logger.getLogger(TextFormat::class.java.name)
        val previousFilter = logger.filter
        logger.filter = Filter { record ->
            when {
                parsing.get() -> false
                previousFilter != null -> previousFilter.isLoggable(record)
                else -> true
            }
        }
    }

    private val parsing = ThreadLocal.withInitial { false }

    fun <T> withWarningsSilenced(block: () -> T): T {
        val previous = parsing.get()
        parsing.set(true)
        try {
            return block()
        } finally {
            if (!previous) {
                parsing.remove()
            }
        }
    }
}
