package org.maiaframework.googlecharts

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class GoogleLineChartDataTableTest {


    @Test
    fun testLineChart() {

        val random = Random(System.currentTimeMillis())
        val builder = GoogleChartsDataTable.aGoogleChartBuilder()

        val xAxisLabel1 = "A"
        val xAxisLabel2 = "B"
        val xAxisLabel3 = "C"

        val expectedColumns = mutableListOf<Map<String, Any>>()
        val expectedRows = mutableListOf<Map<String, Any>>()
        val expectedMap = LinkedHashMap<String, Any>()
        expectedMap["cols"] = expectedColumns
        expectedMap["rows"] = expectedRows

        val columnId1 = UUID.randomUUID().toString()
        val columnId2 = UUID.randomUUID().toString()
        val columnId3 = UUID.randomUUID().toString()

        val columnLabel1 = UUID.randomUUID().toString()
        val columnLabel3 = UUID.randomUUID().toString()

        val valueRow1Col2 = random.nextInt()
        val valueRow1Col3: Int? = null

        val valueRow2Col2 = random.nextDouble()
        val valueRow2Col3 = random.nextInt()

        val valueRow3Col2: Number? = null
        val valueRow3Col3 = random.nextInt()

        builder.addStringColumn(columnId1, columnLabel1)
        expectedColumns.add(mapOf(
                "id" to columnId1,
                "label" to columnLabel1,
                "type" to "string"
        ))

        builder.addBooleanColumn(columnId2, label = null)
        expectedColumns.add(mapOf("id" to columnId2, "type" to "boolean"))

        builder.addNumberColumn(columnId3, columnLabel3)
        expectedColumns.add(mapOf(
                "id" to columnId3,
                "label" to columnLabel3,
                "type" to "number"
        ))

        expectedRows.add(mapOf(
                "c" to listOf(
                        mapOf("v" to xAxisLabel1),
                        mapOf("v" to valueRow1Col2),
                        mapOf("v" to valueRow1Col3)
                )
        ))

        expectedRows.add(mapOf(
                "c" to listOf(
                        mapOf("v" to xAxisLabel2),
                        mapOf("v" to valueRow2Col2),
                        mapOf("v" to valueRow2Col3)
                ))
        )

        expectedRows.add(mapOf(
                "c" to listOf(
                        mapOf("v" to xAxisLabel3),
                        mapOf("v" to valueRow3Col2),
                        mapOf("v" to valueRow3Col3)
                )
        ))

        val dataTable = builder.forLineChart<String>()
                .addDataPoint(xAxisLabel1, columnId2, valueRow1Col2)
                .addDataPoint(xAxisLabel1, columnId3, valueRow1Col3)

                .addDataPoint(xAxisLabel2, columnId2, valueRow2Col2)
                .addDataPoint(xAxisLabel2, columnId3, valueRow2Col3)

                .addDataPoint(xAxisLabel3, columnId2, valueRow3Col2)
                .addDataPoint(xAxisLabel3, columnId3, valueRow3Col3)
                .build()

        val actualMap = dataTable.asMap()

        assertThat(actualMap).isEqualTo(expectedMap)

    }


}
