package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.flags.DataSourceType
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class ElasticsearchEnabledBlotterTest {


    private abstract class ThingSpec : AbstractSpec(AppKey("Test")) {

        protected fun thingEntity() = entity("com.example", "Thing") {
            field("name", FieldTypes.string)
            field("count", FieldTypes.int)
        }

    }


    private class EsAndPlainBlotterSpec : ThingSpec() {

        val thing = thingEntity()

        val enabled = searchableDto(baseName = "ThingEs", entityDef = thing) {
            elasticsearchEnabled(version = 1)
            elasticsearchEnabled(version = 3)
            elasticsearchEnabled(version = 2)
            field("name")
            field("count")
        }

        val plain = searchableDto(baseName = "ThingPlain", entityDef = thing) {
            field("name")
        }

        val enabledBlotter = blotter(enabled) {
            columnFromDto("name")
        }

        val plainBlotter = blotter(plain) {
            columnFromDto("name")
        }

    }


    private class PlainOnlySpec : ThingSpec() {

        val thing = thingEntity()

        val plain = searchableDto(baseName = "ThingPlain", entityDef = thing) {
            field("name")
        }

        val plainBlotter = blotter(plain) {
            columnFromDto("name")
        }

    }


    private class DtoThenManualEsDocSpec : ThingSpec() {

        val thing = thingEntity()

        val alpha = searchableDto(baseName = "Alpha", entityDef = thing) {
            elasticsearchEnabled()
            field("name")
        }

        val manual = esDoc("com.example", "AlphaManual", indexBaseName = "alpha", indexDescription = "manual") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class ManualEsDocThenDtoSpec : ThingSpec() {

        val thing = thingEntity()

        val manual = esDoc("com.example", "AlphaManual", indexBaseName = "alpha", indexDescription = "manual") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

        val alpha = searchableDto(baseName = "Alpha", entityDef = thing) {
            elasticsearchEnabled()
            field("name")
        }

    }


    private class SameClassDifferentIndexSpec : ThingSpec() {

        val thing = thingEntity()

        val alpha = searchableDto(baseName = "Alpha", entityDef = thing) {
            elasticsearchEnabled()
            field("name")
        }

        val manual = esDoc("com.example", "Alpha", indexBaseName = "other_index", indexDescription = "manual") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class DifferentVersionSpec : ThingSpec() {

        val thing = thingEntity()

        val alpha = searchableDto(baseName = "Alpha", entityDef = thing) {
            elasticsearchEnabled()
            field("name")
        }

        val manual = esDoc("com.example", "AlphaManual", indexBaseName = "alpha", esDocVersion = 2, indexDescription = "manual") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private val spec = EsAndPlainBlotterSpec()


    @Test
    fun `model esDocsDefs contains every derived EsDocDef`() {

        val esDocsDefs = spec.modelDef.esDocsDefs

        assertThat(esDocsDefs).containsExactlyInAnyOrderElementsOf(spec.enabled.esDocDefs)
        assertThat(esDocsDefs.map { it.esDocVersion }).containsExactlyInAnyOrder(1, 2, 3)

    }


    @Test
    fun `blotter over ES enabled dto uses the highest version EsDoc source`() {

        val source = spec.enabledBlotter.blotterSourceDef

        assertThat(source).isInstanceOf(BlotterEsDocSourceDef::class.java)
        assertThat((source as BlotterEsDocSourceDef).esDocDef).isSameAs(spec.enabled.esDocDef)
        assertThat(source.esDocDef.esDocVersion).isEqualTo(3)
        assertThat(source.dataSourceType).isEqualTo(DataSourceType.ELASTIC_SEARCH)

    }


    @Test
    fun `blotter over non ES dto is unchanged`() {

        val source = spec.plainBlotter.blotterSourceDef

        assertThat(source).isInstanceOf(BlotterSearchableDtoSourceDef::class.java)
        assertThat(source.dataSourceType).isEqualTo(DataSourceType.DATABASE)

        val plainOnly = PlainOnlySpec()
        assertThat(plainOnly.modelDef.esDocsDefs).isEmpty()

    }


    @Test
    fun `duplicate index and version is rejected when the dto is declared first`() {

        assertThatThrownBy { DtoThenManualEsDocSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("alpha")
            .hasMessageContaining("com.example.Alpha")
            .hasMessageContaining("com.example.AlphaManual")

    }


    @Test
    fun `duplicate index and version is rejected when the manual esDoc is declared first`() {

        assertThatThrownBy { ManualEsDocThenDtoSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("alpha")
            .hasMessageContaining("com.example.Alpha")
            .hasMessageContaining("com.example.AlphaManual")

    }


    @Test
    fun `same class name and version is rejected even with a different index name`() {

        assertThatThrownBy { SameClassDifferentIndexSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("com.example.Alpha")

    }


    @Test
    fun `same index with a different version is accepted`() {

        assertThat(DifferentVersionSpec().modelDef.esDocsDefs).hasSize(2)

    }


    @Test
    fun `ES source resolves dto fields by path`() {

        val source = spec.enabledBlotter.blotterSourceDef
        val field = source.findFieldByPath(FieldPath.of("name"))

        assertThat(field.classFieldName.value).isEqualTo("name")

    }


}
