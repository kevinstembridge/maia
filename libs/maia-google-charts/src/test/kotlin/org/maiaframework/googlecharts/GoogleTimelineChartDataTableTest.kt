package org.maiaframework.googlecharts

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.util.*

class GoogleTimelineChartDataTableTest {

    @Test
    fun testWithOptionalColumnsPopulated() {

        val someRowId = "rowId_" + UUID.randomUUID().toString()
        val someBarId = "barId_" + UUID.randomUUID().toString()
        val someTooltipId = "tooltipId_" + UUID.randomUUID().toString()

        val someRowLabel = "rowLabel_" + UUID.randomUUID().toString()
        val someBarLabel = "barLabel_" + UUID.randomUUID().toString()
        val someTooltipLabel = "tooltipLabel_" + UUID.randomUUID().toString()

        val expectedColumns = ArrayList<Map<String, Any>>()
        val expectedRows = ArrayList<Map<String, Any>>()
        val expectedMap = HashMap<String, Any>()
        expectedMap["cols"] = expectedColumns
        expectedMap["rows"] = expectedRows

        expectedColumns.add(mapOf(
                "id" to someRowId,
                "label" to someRowLabel,
                "type" to "string"
        ))

        expectedColumns.add(mapOf(
                "id" to someBarId,
                "label" to someBarLabel,
                "type" to "string"
        ))

        expectedColumns.add(mapOf(
                "id" to someTooltipId,
                "label" to someTooltipLabel,
                "type" to "string"
        ))

        expectedColumns.add(mapOf(
                "id" to "start",
                "label" to "Start",
                "type" to "date"
        ))

        expectedColumns.add(mapOf(
                "id" to "end",
                "label" to "End",
                "type" to "date"
        ))

        val row1RowLabel = UUID.randomUUID().toString()
        val row1BarLabel = UUID.randomUUID().toString()
        val row1Tooltip = UUID.randomUUID().toString()
        val row1Start = Instant.now()
        val row1End = row1Start.plusSeconds(1)

        val row1StartFormatted = format(row1Start)
        val row1EndFormatted = format(row1End)

        expectedRows.add(mapOf(
                "c" to listOf(
                        mapOf("v", row1RowLabel),
                        mapOf("v", row1BarLabel),
                        mapOf("v", row1Tooltip),
                        mapOf("v", row1StartFormatted),
                        mapOf("v", row1EndFormatted)
                )
        ))

        val builder = GoogleTimeLineChartDataTableBuilder(someRowId, someRowLabel)
                .withBar(someBarId, someBarLabel)
                .withTooltip(someTooltipId, someTooltipLabel)

        builder.addRow(row1RowLabel, row1BarLabel, row1Tooltip, row1Start, row1End)

        val dataTable = builder.build()

        val actualMap = dataTable.asMap()

        assertThat(actualMap).isEqualTo(expectedMap)

    }


    private fun format(instant: Instant): String {

        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))

        return String.format(
                "Date(%d, %d, %d, %d, %d, %d, %d)",
                localDateTime.get(ChronoField.YEAR),
                localDateTime.get(ChronoField.MONTH_OF_YEAR) - 1,
                localDateTime.get(ChronoField.DAY_OF_MONTH),
                localDateTime.get(ChronoField.HOUR_OF_DAY),
                localDateTime.get(ChronoField.MINUTE_OF_HOUR),
                localDateTime.get(ChronoField.SECOND_OF_MINUTE),
                localDateTime.get(ChronoField.MILLI_OF_SECOND))

    }


    private fun mapOf(key: String, value: Any): Map<String, Any> {
        return object : HashMap<String, Any>() {
            init {
                put(key, value)
            }
        }
    }


}
