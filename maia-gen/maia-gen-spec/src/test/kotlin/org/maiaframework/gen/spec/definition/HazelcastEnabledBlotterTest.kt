package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.flags.DataSourceType
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class HazelcastEnabledBlotterTest {


    private abstract class ThingSpec : AbstractSpec(AppKey("Test")) {

        protected fun thingEntity() = entity("com.example", "Thing") {
            field("name", FieldTypes.string)
            field("count", FieldTypes.int)
        }

    }


    private class HazelcastSpec : ThingSpec() {

        val thing = thingEntity()

        val enabled = searchableDto(baseName = "ThingHz", entityDef = thing) {
            hazelcastEnabled(autoRefresh = true)
            field("name")
            field("count")
        }

        val plain = searchableDto(baseName = "ThingPlain", entityDef = thing) {
            field("name")
        }

        val enabledBlotter = blotter(enabled) {
            columnFromDto("name")
            columnFromDto("count")
        }

        val plainBlotter = blotter(plain) {
            columnFromDto("name")
        }

    }


    private class DefaultAutoRefreshSpec : ThingSpec() {

        val thing = thingEntity()

        val enabled = searchableDto(baseName = "ThingHz", entityDef = thing) {
            hazelcastEnabled()
            field("name")
        }

    }


    private class PopulationSpec : ThingSpec() {

        val thing = thingEntity()

        val unset = searchableDto(baseName = "ThingUnset", entityDef = thing) {
            hazelcastEnabled()
            field("name")
        }

        val lazy = searchableDto(baseName = "ThingLazy", entityDef = thing) {
            hazelcastEnabled(population = HazelcastPopulation.LAZY)
            field("name")
        }

    }


    private class MaxRowsSpec : ThingSpec() {

        val thing = thingEntity()

        val unset = searchableDto(baseName = "ThingUnset", entityDef = thing) {
            hazelcastEnabled()
            field("name")
        }

        val capped = searchableDto(baseName = "ThingCapped", entityDef = thing) {
            hazelcastEnabled(maxRows = 250)
            field("name")
        }

    }


    private class ZeroMaxRowsSpec : ThingSpec() {

        val thing = thingEntity()

        val bad = searchableDto(baseName = "ThingZero", entityDef = thing) {
            hazelcastEnabled(maxRows = 0)
            field("name")
        }

    }


    private class NegativeMaxRowsSpec : ThingSpec() {

        val thing = thingEntity()

        val bad = searchableDto(baseName = "ThingNegative", entityDef = thing) {
            hazelcastEnabled(maxRows = -5)
            field("name")
        }

    }


    private class HazelcastThenElasticsearchSpec : ThingSpec() {

        val thing = thingEntity()

        val both = searchableDto(baseName = "ThingBoth", entityDef = thing) {
            hazelcastEnabled()
            elasticsearchEnabled()
            field("name")
        }

    }


    private class ElasticsearchThenHazelcastSpec : ThingSpec() {

        val thing = thingEntity()

        val both = searchableDto(baseName = "ThingBoth", entityDef = thing) {
            elasticsearchEnabled()
            hazelcastEnabled()
            field("name")
        }

    }


    private class InfiniteScrollSpec : ThingSpec() {

        val thing = thingEntity()

        val enabled = searchableDto(baseName = "ThingHz", entityDef = thing) {
            hazelcastEnabled()
            field("name")
        }

        val blotter = blotter(enabled) {
            columnFromDto("name")
            infiniteScroll()
        }

    }


    private class PaginationSpec : ThingSpec() {

        val thing = thingEntity()

        val enabled = searchableDto(baseName = "ThingHz", entityDef = thing) {
            hazelcastEnabled()
            field("name")
        }

        val blotter = blotter(enabled) {
            columnFromDto("name")
            pagination(50)
        }

    }


    @Test
    fun `hazelcastEnabled marks the dto as document backed with auto refresh`() {

        val spec = HazelcastSpec()

        assertThat(spec.enabled.isDocumentBacked).isTrue()
        assertThat(spec.enabled.hasAutoRefresh).isTrue()
        assertThat(spec.enabled.hazelcastSpec).isNotNull()
        assertThat(spec.enabled.esVersions).isEmpty()
        assertThat(spec.plain.isDocumentBacked).isFalse()
        assertThat(spec.plain.hazelcastSpec).isNull()

    }


    @Test
    fun `population has no per-dto override unless declared`() {

        val spec = PopulationSpec()

        assertThat(spec.unset.hazelcastSpec!!.population).isNull()
        assertThat(spec.lazy.hazelcastSpec!!.population).isEqualTo(HazelcastPopulation.LAZY)

    }


    @Test
    fun `maxRows defaults to 10000 and can be set per dto`() {

        val spec = MaxRowsSpec()

        assertThat(spec.unset.hazelcastSpec!!.maxRows).isEqualTo(10_000)
        assertThat(spec.capped.hazelcastSpec!!.maxRows).isEqualTo(250)

    }


    @Test
    fun `a zero maxRows is rejected`() {

        assertThatThrownBy { ZeroMaxRowsSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("ThingZero")
            .hasMessageContaining("maxRows")

    }


    @Test
    fun `a negative maxRows is rejected`() {

        assertThatThrownBy { NegativeMaxRowsSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("ThingNegative")
            .hasMessageContaining("maxRows")

    }


    @Test
    fun `autoRefresh defaults to true`() {

        assertThat(DefaultAutoRefreshSpec().enabled.hazelcastSpec!!.autoRefresh).isTrue()

    }


    @Test
    fun `blotter over a hazelcast enabled dto is client side and not paginated`() {

        val spec = HazelcastSpec()
        val source = spec.enabledBlotter.blotterSourceDef

        assertThat(source).isInstanceOf(BlotterHazelcastSourceDef::class.java)
        assertThat(source.dataSourceType).isEqualTo(DataSourceType.HAZELCAST)
        assertThat(spec.enabledBlotter.isPaginated).isFalse()
        assertThat(spec.enabledBlotter.searchableDtoDef).isNull()
        assertThat(spec.plainBlotter.blotterSourceDef).isInstanceOf(BlotterSearchableDtoSourceDef::class.java)
        assertThat(spec.plainBlotter.isPaginated).isTrue()

    }


    @Test
    fun `hazelcast blotter resolves dto fields by path`() {

        val field = HazelcastSpec().enabledBlotter.blotterSourceDef.findFieldByPath(FieldPath.of("name"))

        assertThat(field.classFieldName.value).isEqualTo("name")

    }


    @Test
    fun `hazelcastEnabled then elasticsearchEnabled is rejected`() {

        assertThatThrownBy { HazelcastThenElasticsearchSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("ThingBoth")
            .hasMessageContaining("hazelcastEnabled")
            .hasMessageContaining("elasticsearchEnabled")

    }


    @Test
    fun `elasticsearchEnabled then hazelcastEnabled is rejected`() {

        assertThatThrownBy { ElasticsearchThenHazelcastSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("ThingBoth")
            .hasMessageContaining("hazelcastEnabled")
            .hasMessageContaining("elasticsearchEnabled")

    }


    @Test
    fun `infiniteScroll on a hazelcast blotter is rejected`() {

        assertThatThrownBy { InfiniteScrollSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("infiniteScroll")
            .hasMessageContaining("ThingHz")

    }


    @Test
    fun `pagination on a hazelcast blotter is rejected`() {

        assertThatThrownBy { PaginationSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("pagination")
            .hasMessageContaining("ThingHz")

    }


}
