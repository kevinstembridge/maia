package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.singleKotlinFileIn
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class ElasticSearchDtoSearchServiceRendererTest {


    private class BlotterSpec : AbstractSpec(AppKey("Test")) {

        val widget = entity("com.example", "Widget") {
            field("title", FieldTypes.string)
            crud {}
        }

        val widgetEsDto = searchableDto(baseName = "WidgetEs", entityDef = widget) {
            elasticsearchEnabled()
            field("title")
        }

        val widgetEsBlotter = blotter(widgetEsDto) {
            columnFromDto("id")
            columnFromDto("title")
        }

    }


    private val spec = BlotterSpec()


    @Test
    fun `search and count both use the single mapping function`(@TempDir dir: File) {

        val blotterDef = spec.widgetEsBlotter
        val esDocDef = (blotterDef.blotterSourceDef as BlotterEsDocSourceDef).esDocDef

        ElasticSearchDtoSearchServiceRenderer(blotterDef.searchDtoDef, esDocDef).renderToDir(dir)
        val source = singleKotlinFileIn(dir)

        val searchFunction = source.substringAfter("fun search(").substringBefore("fun count(")
        val countFunction = source.substringAfter("fun count(")

        assertThat(searchFunction).contains("WidgetEsBlotterRowDtoFieldNameMapper.mappingFunction,")
        assertThat(searchFunction).doesNotContain("sortMappingFunction", "sortFieldNameMapper")
        assertThat(countFunction).contains("WidgetEsBlotterRowDtoFieldNameMapper.mappingFunction")

    }


    @Test
    fun `index name is resolved from the esDoc base name and version, not from the EsIndex class`(@TempDir dir: File) {

        val blotterDef = spec.widgetEsBlotter
        val esDocDef = (blotterDef.blotterSourceDef as BlotterEsDocSourceDef).esDocDef

        ElasticSearchDtoSearchServiceRenderer(blotterDef.searchDtoDef, esDocDef).renderToDir(dir)
        val source = singleKotlinFileIn(dir)

        assertThat(source)
            .contains("private val indexNameFactory: IndexNameFactory")
            .contains("private val indexName = this.indexNameFactory.indexNameFrom(WidgetEsEsIndexMeta_v0001.indexBaseNameAndVersion)")
            .doesNotContain("EsIndex,", "esIndex")

    }


}
