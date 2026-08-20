package top.fifthlight.intellij.kotlinhmpp

import com.google.protobuf.TextFormat
import top.fifthlight.intellij.kotlinhmpp.proto.HmppInfoProtos
import java.nio.file.Path
import kotlin.io.path.bufferedReader

/**
 * Parser for HmppInfoProtos.TargetIdeInfo
 */
object HmppInfoParser {
    fun parse(file: Path): HmppInfoProtos.TargetIdeInfo? = runCatching {
        file.bufferedReader().useLines { lines ->
            val builder = StringBuilder()
            var parse = false
            for (line in lines) {
                if (line.startsWith(Consts.KOTLIN_COMMON_TARGET_INFO)) {
                    parse = true
                }
                builder.appendLine(line)
            }
            if (!parse) return@useLines null
            TextFormatLogSilencer.withWarningsSilenced {
                HmppInfoProtos.TargetIdeInfo.newBuilder().apply {
                    TextFormat.Parser
                        .newBuilder()
                        .setAllowUnknownFields(true)
                        .build()
                        .merge(builder.toString(), this)
                }.build()
            }
        }
    }.getOrNull()
}
