package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import top.fifthlight.armorstand.ui.state.DatabaseScreenState
import kotlin.math.max

class ResultTable(
    private val textRenderer: Font,
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
) : AbstractWidget(x, y, width, height, Component.empty()) {
    private var layout: Layout? = null

    private sealed class Layout {
        abstract fun render(
            table: ResultTable,
            graphics: GuiGraphics,
        )

        data object Empty : Layout() {
            override fun render(
                table: ResultTable,
                graphics: GuiGraphics,
            ) {
                graphics.drawString(
                    table.textRenderer,
                    Component.translatable("armorstand.debug_database.empty_tip"),
                    table.x,
                    table.y,
                    CommonColors.WHITE,
                    false,
                )
            }
        }

        data object Loading : Layout() {
            override fun render(
                table: ResultTable,
                graphics: GuiGraphics,
            ) {
                graphics.drawString(
                    table.textRenderer,
                    Component.translatable("armorstand.debug_database.querying"),
                    table.x,
                    table.y,
                    CommonColors.WHITE,
                    false,
                )
            }
        }

        data class Failed(val message: Component?) : Layout() {
            override fun render(
                table: ResultTable,
                graphics: GuiGraphics,
            ) {
                graphics.drawWordWrap(
                    table.textRenderer,
                    message ?: Component.translatable("armorstand.debug_database.query_failed"),
                    table.x,
                    table.y,
                    table.width,
                    CommonColors.WHITE,
                    false,
                )
            }
        }

        data class Updated(val message: Component) : Layout() {
            override fun render(
                table: ResultTable,
                graphics: GuiGraphics,
            ) {
                graphics.drawString(
                    table.textRenderer,
                    message,
                    table.x,
                    table.y,
                    CommonColors.WHITE,
                    false,
                )
            }
        }

        data class Result(
            val duration: Component,
            val headers: List<Component>,
            val rows: List<List<Component>>,
            val columnWidths: List<Int>,
        ) : Layout() {
            override fun render(
                table: ResultTable,
                graphics: GuiGraphics,
            ) {
                val textRenderer = table.textRenderer
                val x = table.x
                val y = table.y
                var offsetX = 0
                var offsetY = 0
                // render duration
                graphics.drawString(
                    textRenderer,
                    duration,
                    x,
                    y,
                    CommonColors.WHITE,
                    false,
                )
                offsetY += textRenderer.lineHeight + 2
                // render headers
                for ((index, header) in headers.withIndex()) {
                    graphics.drawString(
                        textRenderer,
                        header,
                        x + offsetX,
                        y + offsetY,
                        CommonColors.WHITE,
                        false,
                    )
                    offsetX += columnWidths[index] + 2
                }
                graphics.hLine(x, x + offsetX, y + offsetY + textRenderer.lineHeight, CommonColors.WHITE)
                // render rows
                offsetX = 0
                offsetY += textRenderer.lineHeight + 2
                for (row in rows) {
                    for ((index, column) in row.withIndex()) {
                        graphics.drawString(
                            textRenderer,
                            column,
                            x + offsetX,
                            y + offsetY,
                            CommonColors.WHITE,
                            false,
                        )
                        offsetX += columnWidths[index] + 2
                    }
                    offsetX = 0
                    offsetY += textRenderer.lineHeight + 2
                }
            }
        }
    }

    fun setContent(state: DatabaseScreenState.QueryState) {
        layout = when (state) {
            DatabaseScreenState.QueryState.Empty -> Layout.Empty

            DatabaseScreenState.QueryState.Loading -> Layout.Loading

            is DatabaseScreenState.QueryState.Failed -> Layout.Failed(
                message = state.error?.let {
                    Component.translatable("armorstand.debug_database.query_failed_with_message", it)
                }
            )

            is DatabaseScreenState.QueryState.Updated -> Layout.Updated(
                message = Component.translatable(
                    "armorstand.debug_database.executed",
                    state.updateCount,
                    state.duration.toString()
                )
            )

            is DatabaseScreenState.QueryState.Result -> {
                val headerTexts = state.headers.map { Component.literal(it) }
                val rowTexts = state.rows.map { row -> row.map { Component.literal(it) } }
                val columnWidths = mutableListOf<Int>().apply {
                    headerTexts.forEach { add(textRenderer.width(it)) }
                }
                for (row in rowTexts) {
                    for ((index, column) in row.withIndex()) {
                        columnWidths[index] = max(columnWidths[index], textRenderer.width(column))
                    }
                }
                Layout.Result(
                    duration = Component.translatable("armorstand.debug_database.queried_time", state.duration.toString()),
                    headers = headerTexts,
                    rows = rowTexts,
                    columnWidths = columnWidths,
                )
            }
        }
    }

    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        layout?.render(this, graphics)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
}
