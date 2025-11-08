package org.maiaframework.elasticsearch.index

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EsIndexNameFactoryTest {

    private val indexNameFactory = EsIndexNameFactory()


    @ParameterizedTest
    @CsvSource(
        "abc_v0001, abc, 1",
        "def_v20000, def, 20000",
        "ghi, ghi, null"
    )
    fun testIndexBaseNameFrom(
            indexName: String,
            expectedBaseName: String,
            versionString: String
    ) {

        val version = if (versionString == "null") {
            1
        } else {
            versionString.toInt()
        }

        val actual = indexNameFactory.indexNameFrom(indexName)
        val expected = EsIndexName(EsIndexBaseName(expectedBaseName), EsIndexVersion(version))
        assertThat(actual).isEqualTo(expected)

    }


}
