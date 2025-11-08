package org.maiaframework.googlecharts

import org.maiaframework.googlecharts.JavascriptDateFormatter.format
import java.time.Instant
import java.util.*

class GoogleTimeLineChartDataTableBuilder(private val rowId: String, private val rowLabel: String) {


    private val rows = ArrayList<List<Any>>()
    private var barId: String? = null
    private var barLabel: String? = null
    private var tooltipId: String? = null
    private var tooltipLabel: String? = null


    init {

        require(rowId.isNotBlank()) { "rowId must not be blank" }
        require(rowLabel.isNotBlank()) { "rowLabel must not be blank" }

    }


    fun withBar(barId: String, barLabel: String): GoogleTimeLineChartDataTableBuilder {

        this.barId = barId
        this.barLabel = barLabel
        return this

    }


    fun withTooltip(tooltipId: String, tooltipLabel: String): GoogleTimeLineChartDataTableBuilder {

        this.tooltipId = tooltipId
        this.tooltipLabel = tooltipLabel
        return this

    }


    fun build(): GoogleChartsDataTable {

        val builder = GoogleChartsDataTable.aGoogleChartBuilder()
        builder.addStringColumn(this.rowId, this.rowLabel)

        if (this.barId != null || this.barLabel != null) {
            builder.addStringColumn(this.barId!!, this.barLabel!!)
        }

        if (this.tooltipId != null || this.tooltipLabel != null) {
            builder.addStringColumn(this.tooltipId!!, this.tooltipLabel!!)
        }

        builder.addDateColumn("start", "Start")
        builder.addDateColumn("end", "End")

        return GoogleChartsDataTable(builder.getColumnDefs(), this.rows)

    }


    fun addRow(rowLabel: String, start: Instant, end: Instant) {

        require(rowLabel.isNotBlank()) { "rowLabel must not be blank" }

        this.rows.add(listOf<Any>(rowLabel, format(start), format(end)))

    }


    fun addRow(rowLabel: String, barLabel: String, start: Instant, end: Instant) {

        require(rowLabel.isNotBlank()) { "rowLabel must not be blank" }

        this.rows.add(listOf<Any>(rowLabel, barLabel, format(start), format(end)))

    }


    fun addRow(rowLabel: String, barLabel: String, tooltip: String, start: Instant, end: Instant) {

        require(rowLabel.isNotBlank()) { "rowLabel must not be blank" }

        this.rows.add(listOf<Any>(rowLabel, barLabel, tooltip, format(start), format(end)))

    }


}
