package org.maiaframework.googlecharts

import java.util.*
import java.util.function.Function

class GoogleLineChartDataTableBuilder<DOMAIN : Comparable<DOMAIN>> internal constructor(
        private val columnDefs: List<ColumnDef>,
        domainLabelComparator: Comparator<DOMAIN>) {


    private val dataPoints: MutableMap<DOMAIN, MutableMap<String, Any?>>

    private var domainLabelFormatter: Function<in DOMAIN, String> = Function { it.toString() }


    init {

        if (columnDefs.isEmpty()) {
            throw IllegalArgumentException("The list of columnDefs must not be empty")
        }

        this.dataPoints = TreeMap(domainLabelComparator)

    }


    fun withDomainLabelFormatter(domainLabelFormatter: Function<DOMAIN, String>): GoogleLineChartDataTableBuilder<DOMAIN> {

        this.domainLabelFormatter = domainLabelFormatter
        return this

    }


    fun build(): GoogleChartsDataTable {

        val rows = convertDataPointsToRows()
        return GoogleChartsDataTable(this.columnDefs, rows)

    }


    private fun convertDataPointsToRows(): List<List<Any?>> {

        return this.dataPoints
                .entries
                .map { entry -> ArrayList(entry.value.values) }

    }


    fun addDataPoint(xAxisLabel: DOMAIN, plotName: String, value: Number?): GoogleLineChartDataTableBuilder<DOMAIN> {

        val valueByPlotName = this.dataPoints.getOrPut(xAxisLabel) { createMapForRow(xAxisLabel) }
        valueByPlotName[plotName] = value
        return this

    }


    fun addDataPoint(xAxisLabel: DOMAIN, plotName: String, value: String): GoogleLineChartDataTableBuilder<DOMAIN> {

        val valueByPlotName = this.dataPoints.getOrPut(xAxisLabel) { createMapForRow(xAxisLabel) }
        valueByPlotName[plotName] = value
        return this

    }


    private fun createMapForRow(xAxisLabel: DOMAIN): MutableMap<String, Any?> {

        val map = LinkedHashMap<String, Any?>()

        val firstColumn = this.columnDefs.first()
        map[firstColumn.id!!] = this.domainLabelFormatter.apply(xAxisLabel)

        this.columnDefs
                .stream()
                .skip(1)
                .forEach { columnDef ->
                    val s: String = columnDef.id ?: throw RuntimeException("column def has no id: $columnDef")
                    map[s] = null
                }

        this.columnDefs
                .stream()
                .skip(1)
                .forEach { columnDef -> map[columnDef.id ?: throw RuntimeException("column def has no id: $columnDef") ] = null }

        return map

    }


}
