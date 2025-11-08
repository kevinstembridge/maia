package org.maiaframework.data_row

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class FixedWidthDataRowParserTest {


    @ParameterizedTest
    @MethodSource("source")
    fun `test fixed-width columns`(
        columnWidths: List<Int>,
        line: String,
        expected: List<String?>
    ) {

        val actual = parseDataRow(columnWidths).invoke(line)
        assertThat(actual).isEqualTo(expected)

    }


    companion object {

        @JvmStatic
        fun source(): Stream<Arguments> {

            return Stream.of(
                arguments(listOf(8, 9, 17), "one     ,two      ,three            ,", listOf("one     ", "two      ", "three            "))
            )

        }

    }


}
