package org.maiaframework.elasticsearch.index

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class EsIndexNameTest {


    @ParameterizedTest
    @CsvSource(
            "abc, 1, abc_v0001",
            "def, 20000, def_v20000",
            "ghi, null, ghi_v0001"
    )
    fun testEsIndexName(baseName: String, versionString: String, expected: String) {

        val version = if (versionString == "null") {
            1
        } else {
            versionString.toInt()
        }

        val esIndexBaseName = EsIndexBaseName(baseName)
        val esIndexVersion = EsIndexVersion(version)
        val actual = EsIndexName(esIndexBaseName, esIndexVersion).asString
        assertThat(actual).isEqualTo(expected)

    }


}
