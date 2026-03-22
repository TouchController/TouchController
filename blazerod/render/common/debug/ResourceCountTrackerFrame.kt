package top.fifthlight.blazerod.render.common.debug

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel

class ResourceCountTrackerFrame : JFrame("Resource Count Tracker") {
    private val tableModel = object : DefaultTableModel(arrayOf("Resource ID", "Count"), 0) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    private val table = JTable(tableModel)
    private val updateTimer = Timer(1000) { updateData() }
    private val emptyLabel = JLabel("Tracker not initialized", SwingConstants.CENTER).apply {
        font = font.deriveFont(Font.BOLD)
    }
    private val scrollPane = JScrollPane(table)

    init {
        setupUI()
        setupListeners()
        startTracking()
    }

    private fun setupUI() {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        preferredSize = Dimension(600, 400)
        layout = BorderLayout()

        table.fillsViewportHeight = true

        pack()
    }

    private fun setupListeners() {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                updateTimer.stop()
            }
        })
    }

    private fun startTracking() {
        updateData()
        updateTimer.start()
    }

    private fun updateData() {
        ResourceCountTracker.instance?.let { tracker ->
            add(scrollPane, BorderLayout.CENTER)
            tableModel.rowCount = 0
            tracker.dumpData()
                .asSequence()
                .sortedBy { (key, _) -> key }
                .forEach { (key, value) ->
                    tableModel.addRow(arrayOf(key.toString(), value.toString()))
                }
        } ?: run {
            add(emptyLabel, BorderLayout.CENTER)
        }
    }
}
