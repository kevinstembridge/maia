package org.maiaframework.googlecharts

import java.util.*

class GoogleChartsDataTable(columnDefs: List<ColumnDef>, rows: List<List<Any?>>) {


    private val dataTable: Map<String, List<Map<String, Any?>>>


    init {

        val columns = buildColumns(columnDefs)
        val formattedRows = buildRows(rows)

        this.dataTable = object : LinkedHashMap<String, List<Map<String, Any?>>>() {
            init {
                put("cols", columns)
                put("rows", formattedRows)
            }
        }


    }


    private fun buildColumns(columnDefs: List<ColumnDef>): List<Map<String, Any>> {

        return columnDefs.map { columnDef ->

            val columnMap = mutableMapOf<String, Any>()
            columnMap["type"] = columnDef.type
            columnDef.id?.let { id -> columnMap.put("id", id) }
            columnDef.label?.let { label -> columnMap.put("label", label) }
            columnDef.role?.let { role -> columnMap.put("role", role) }
            columnMap

        }

    }


    private fun buildRows(rows: List<List<Any?>>): List<Map<String, Any?>> {

        return rows.map { row ->

            val rowValues = row.map { value -> mapOf("v" to value) }
            mapOf("c" to rowValues)

        }

    }


    fun asMap(): Map<String, List<Map<String, Any?>>> {
        return LinkedHashMap(this.dataTable)
    }


    class Builder {


        private val columnDefs = ArrayList<ColumnDef>()


        fun addStringColumn(id: String, label: String?, role: String? = null): Builder {

            addColumn(id, label, "string", role)
            return this

        }


        fun addBooleanColumn(id: String, label: String?, role: String? = null): Builder {

            addColumn(id, label, "boolean", role)
            return this

        }


        fun addDateColumn(id: String, label: String?, role: String? = null): Builder {

            addColumn(id, label, "date", role)
            return this

        }


        fun addNumberColumn(id: String, label: String?, role: String? = null): Builder {

            addColumn(id, label, "number", role)
            return this

        }


        private fun addColumn(id: String?, label: String?, type: String, role: String?) {

            this.columnDefs.add(ColumnDef(id, label, type, role))

        }


        fun <DOMAIN : Comparable<DOMAIN>> forLineChart(): GoogleLineChartDataTableBuilder<DOMAIN> {

            return GoogleLineChartDataTableBuilder(this.columnDefs, Comparator.naturalOrder())

        }


        fun <DOMAIN : Comparable<DOMAIN>> forLineChart(domainLabelComparator: Comparator<DOMAIN>): GoogleLineChartDataTableBuilder<*> {

            return GoogleLineChartDataTableBuilder(this.columnDefs, domainLabelComparator)

        }


        fun getColumnDefs(): List<ColumnDef> {

            return ArrayList(this.columnDefs)

        }


    }


    companion object {


        fun aGoogleChartBuilder(): GoogleChartsDataTable.Builder {

            return Builder()

        }

    }


}
